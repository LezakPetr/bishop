package bishop.engine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import parallel.Parallel;

import bishop.base.BitBoard;
import bishop.base.Color;
import bishop.base.HandlerRegistrarImpl;
import bishop.base.Holder;
import bishop.base.IHandlerRegistrar;
import bishop.base.IMoveWalker;
import bishop.base.LegalMoveFinder;
import bishop.base.Move;
import bishop.base.MoveList;
import bishop.base.MoveStack;
import bishop.base.MoveType;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Position;
import bishop.base.PseudoLegalMoveGenerator;
import bishop.base.QuiescencePseudoLegalMoveGenerator;
import bishop.base.Square;

public final class SerialSearchEngine implements ISearchEngine {

	private static class NodeRecord implements ISearchResult {
		public final Move currentMove;
		public int moveListBegin;
		public int moveListEnd;
		public final MoveList principalVariation;
		public final NodeEvaluation evaluation;
		public final Move killerMove;
		public final Move principalMove;
		public int hashBestCompressedMove;
		public int legalMoveCount;
		public boolean allMovesGenerated;
		public boolean isCheck;
		public boolean checkCalculated;
		public int maxExtension;
		public final AttackCalculator attackCalculator;

		public NodeRecord(final int maxPrincipalDepth) {
			currentMove = new Move();
			principalVariation = new MoveList(maxPrincipalDepth);
			evaluation = new NodeEvaluation();
			killerMove = new Move();
			principalMove = new Move();
			hashBestCompressedMove = Move.NONE_COMPRESSED_MOVE;
			attackCalculator = new AttackCalculator();
		}

		public void openNode(final int alpha, final int beta) {
			this.evaluation.setEvaluation(Evaluation.MIN);
			this.evaluation.setAlpha(alpha);
			this.evaluation.setBeta(beta);
			this.legalMoveCount = 0;
			this.allMovesGenerated = false;
			this.checkCalculated = false;
		}
		
		public NodeEvaluation getNodeEvaluation() {
			return evaluation;
		}
		
		public MoveList getPrincipalVariation() {
			return principalVariation;
		}
	}

	private class MoveWalker implements IMoveWalker {
		public boolean processMove(final Move move) {
			int estimate = historyTable[move.getBeginSquare()][move.getTargetSquare()];
			
			final int movingPieceType = move.getMovingPieceType();
			final int capturedPieceType = move.getCapturedPieceType();

			estimate += Utils.estimateCapture(movingPieceType, capturedPieceType); 

			final NodeRecord nodeRecord = nodeStack[currentDepth];

			if (move.equals(nodeRecord.killerMove))
				estimate += KILLER_MOVE_ESTIMATE;

			if (move.equals(nodeRecord.principalMove))
				estimate += PRINCIPAL_MOVE_ESTIMATE;

			if (move.getCompressedMove() == nodeRecord.hashBestCompressedMove && nodeRecord.hashBestCompressedMove != Move.NONE_COMPRESSED_MOVE)
				estimate += HASH_BEST_MOVE_ESTIMATE;
			
			moveStack.setRecord(moveStackTop, move, estimate);
			moveStackTop++;

			return true;
		}
	};

	private static final int KILLER_MOVE_ESTIMATE = 5 * PieceTypeEvaluations.PAWN_EVALUATION;
	private static final int PRINCIPAL_MOVE_ESTIMATE = 10 * PieceTypeEvaluations.PAWN_EVALUATION;
	private static final int HASH_BEST_MOVE_ESTIMATE = 30 * PieceTypeEvaluations.PAWN_EVALUATION;
	private static final int MIN_PARALLEL_HORIZON = 3 * ISearchEngine.HORIZON_GRANULARITY;
	
	// Settings
	private int maxTotalDepth;
	private SearchSettings searchSettings;
	private IHashTable hashTable;

	// Actual task
	private NodeRecord[] nodeStack;
	private MoveStack moveStack;
	private final Position currentPosition;
	private int currentDepth;
	private int depthAdvance;
	private int closeToDepth;
	private int moveStackTop;
	private long nodeCount;
	private final RepeatedPositionRegister repeatedPositionRegister;
	private final EvaluatedMoveList evaluatedMoveList;

	// Synchronization
	private EngineState engineState;
	private final Object monitor;
	private final Parallel parallel;

	// Supplementary objects
	private final QuiescencePseudoLegalMoveGenerator quiescenceLegalMoveGenerator;
	private final PseudoLegalMoveGenerator pseudoLegalMoveGenerator;
	private final LegalMoveFinder legalMoveFinder;
	private final MoveWalker moveWalker;
	private final int[][] historyTable;
	private final FinitePositionEvaluator finiteEvaluator;
	private final SearchExtensionCalculator extensionCalculator;
	private final MoveExtensionEvaluator moveExtensionEvaluator;
	private SearchTask task;
	private IPositionEvaluator positionEvaluator;
	private final List<ISearchEngine> runningChildEngineList;
	private final List<ISearchEngine> idleChildEngineList;
	private final List<SearchTask> subTaskList;
	private final HandlerRegistrarImpl<ISearchEngineHandler> handlerRegistrar;

	
	public SerialSearchEngine(final Parallel parallel, final List<ISearchEngine> childEngineList) {
		this.parallel = parallel;
		this.runningChildEngineList = new ArrayList<>(childEngineList);
		this.idleChildEngineList = new ArrayList<>(childEngineList);
		this.subTaskList = new ArrayList<>();
		this.evaluatedMoveList = new EvaluatedMoveList(PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION);
		this.handlerRegistrar = new HandlerRegistrarImpl<>();
		
		moveWalker = new MoveWalker();

		quiescenceLegalMoveGenerator = new QuiescencePseudoLegalMoveGenerator();
		quiescenceLegalMoveGenerator.setWalker(moveWalker);

		pseudoLegalMoveGenerator = new PseudoLegalMoveGenerator();
		pseudoLegalMoveGenerator.setWalker(moveWalker);
		
		legalMoveFinder = new LegalMoveFinder();

		engineState = EngineState.STOPPED;
		monitor = new Object();
		depthAdvance = 0;
		historyTable = new int[Square.LAST][Square.LAST];
		
		currentPosition = new Position();
		repeatedPositionRegister = new RepeatedPositionRegister();
		
		finiteEvaluator = new FinitePositionEvaluator();
		finiteEvaluator.setRepeatedPositionRegister(repeatedPositionRegister);
		
		extensionCalculator = new SearchExtensionCalculator();
		moveExtensionEvaluator = new MoveExtensionEvaluator();
		
		setHashTable(new NullHashTable());

		task = null;
	}

	/**
	 * Checks if engine is in one of given expected states. If not exception is
	 * thrown. Expects that calling thread owns the monitor.
	 * @param expectedState expected engine state
	 */
	private void checkEngineState(final EngineState... expectedStates) {
		for (EngineState state : expectedStates) {
			if (state == engineState)
				return;
		}

		throw new RuntimeException("Engine is not in expected state, but in state " + engineState.name());
	}
	
	private void receiveUpdates() {
		if ((nodeCount & 0xFFF) == 0) {
			synchronized (monitor) {
				if (task.isTerminated())
					throw new SearchTerminatedException();
				
				updateBoundariesInNodes();
			}
		}
	}
	
	private boolean isNullSearchPossible() {
		if (currentDepth > 0) {
			final Move lastMove = nodeStack[currentDepth - 1].currentMove;
			
			if (lastMove.getMoveType() == MoveType.NULL)
				return false;
		}
		
		final int beta = nodeStack[currentDepth].evaluation.getBeta();
		
		if (beta > Evaluation.MATE_ZERO_DEPTH)
			return false;
		
		if (getIsCheck())
			return false;
		
		// Check position - at least two figures are needed
		final int onTurn = currentPosition.getOnTurn();
		long figureMask = BitBoard.EMPTY;
		
		for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
			figureMask |= currentPosition.getPiecesMask(onTurn, pieceType);
		}
		
		final int figureCount = BitBoard.getSquareCount(figureMask);
		
		return figureCount > 1;
	}
	
	private void alphaBeta (final int horizon) {
		receiveUpdates();
		
		final NodeRecord currentRecord = nodeStack[currentDepth];
		final int onTurn = currentPosition.getOnTurn();
		final boolean isQuiescenceSearch = (horizon < ISearchEngine.HORIZON_GRANULARITY);
		final int maxCheckSearchDepth = searchSettings.getMaxCheckSearchDepth();
		final boolean isCheckSearch = isQuiescenceSearch && horizon > -maxCheckSearchDepth && getIsCheck();
		final boolean isLegalPosition = !currentPosition.isSquareAttacked(onTurn, currentPosition.getKingPosition(Color.getOppositeColor(onTurn)));
		final int mateEvaluation = Evaluation.getMateEvaluation(currentDepth + depthAdvance);
		
		currentRecord.principalVariation.clear();

		if (isLegalPosition) {
			final int initialAlpha = currentRecord.evaluation.getAlpha();
			final int initialBeta = currentRecord.evaluation.getBeta();
			
			if (checkFiniteEvaluation(horizon, currentRecord, initialAlpha, initialBeta))
				return;
			
			final boolean winMateRequired = isWinMateSearch(initialAlpha);
			final boolean loseMateRequired = isLoseMateSearch(initialBeta);
			final boolean mateRequired = winMateRequired || loseMateRequired;
						
			currentRecord.moveListBegin = moveStackTop;
			
			// Try to find position in hash table
			final HashRecord hashRecord = new HashRecord();
			
			if (updateRecordByHash(horizon, currentRecord, initialAlpha, initialBeta, hashRecord))
				return;
			
			// Evaluate position
			final int absoluteAlpha;
			final int absoluteBeta;
			
			if (onTurn == Color.WHITE) {
				absoluteAlpha = initialAlpha;
				absoluteBeta = initialBeta;
			}
			else {
				absoluteAlpha = -initialBeta;
				absoluteBeta = -initialAlpha;				
			}

			final int whitePositionEvaluation = positionEvaluator.evaluatePosition(currentPosition, absoluteAlpha, absoluteBeta, currentRecord.attackCalculator);
			final int positionEvaluation = Evaluation.getRelative(whitePositionEvaluation, onTurn);
			
			final int positionExtension;
			
			if (horizon >= searchSettings.getMinExtensionHorizon()) {
				final boolean isCheck = getIsCheck();
				
				positionExtension = extensionCalculator.getExtension(currentPosition, isCheck, hashRecord, horizon, currentRecord.attackCalculator);
			}
			else
				positionExtension = 0;

			final boolean isMaxDepth = (currentDepth >= maxTotalDepth - 1);
			
			// Use position evaluation as initial evaluation
			if ((isQuiescenceSearch && !isCheckSearch) || isMaxDepth) {
				currentRecord.evaluation.setEvaluation(positionEvaluation);
				
				nodeCount++;

				if (currentRecord.evaluation.updateBoundaries (positionEvaluation)) {
					currentRecord.evaluation.setAlpha(positionEvaluation);

					return;
				}
			}

			final int maxQuiescenceDepth = searchSettings.getMaxQuiescenceDepth();
			
			if (!isMaxDepth && horizon > -maxQuiescenceDepth && (!isQuiescenceSearch || isCheckSearch || !mateRequired)) {
				final NodeRecord nextRecord = nodeStack[currentDepth + 1];
				
				currentRecord.moveListBegin = moveStackTop;
				currentRecord.moveListEnd = moveStackTop;
				
				// Null move heuristic
				if (!isQuiescenceSearch && isNullSearchPossible()) {
					int nullHorizon = horizon - searchSettings.getNullMoveReduction();
					final Move move = new Move();
					move.createNull(currentPosition.getCastlingRights().getIndex(), currentPosition.getEpFile());
					
					evaluateMove(move, nullHorizon, 0, initialBeta, initialBeta + 1, false);
					
					final NodeEvaluation parentEvaluation = nextRecord.evaluation.getParent();
					
					if (parentEvaluation.getEvaluation() > initialBeta) {
						currentRecord.evaluation.update(parentEvaluation);
						return;
					}
				}
				
				// Try P-var move first
				final Move precalculatedMove = new Move();
				final boolean precalculatedMoveFound;
				boolean precalculatedBetaCutoff = false;
				
				if (currentRecord.hashBestCompressedMove != Move.NONE_COMPRESSED_MOVE) {
					precalculatedMoveFound = precalculatedMove.uncompressMove(currentRecord.hashBestCompressedMove, currentPosition);
				}
				else {
					precalculatedMoveFound = precalculatedMove.uncompressMove(currentRecord.principalMove.getCompressedMove(), currentPosition);
				}
				
				if (precalculatedMoveFound) {
					final int alpha = currentRecord.evaluation.getAlpha();
					final int beta = currentRecord.evaluation.getBeta();
					
					evaluateMove(precalculatedMove, horizon, positionExtension, alpha, beta, false);
					
					if (currentDepth > closeToDepth)
						return;
					else
						closeToDepth = Integer.MAX_VALUE;
					
					if (updateCurrentRecordAfterEvaluation(precalculatedMove, horizon, currentRecord, nextRecord)) {
						precalculatedBetaCutoff = true;
					}
				}

				// If P-var move didn't make beta cutoff try other moves
				if (!precalculatedBetaCutoff) {
					generateAndSortMoves(currentRecord, horizon, isQuiescenceSearch, isCheckSearch);
					
					// First legal move
					while (currentRecord.moveListEnd > currentRecord.moveListBegin) {
						final int alpha = currentRecord.evaluation.getAlpha();
						final int beta = currentRecord.evaluation.getBeta();

						if (currentRecord.legalMoveCount == 0 || beta - alpha == 1)
							break;
						
						final Move move = new Move();
						moveStack.getMove(currentRecord.moveListEnd - 1, move);
						
						if (!move.equals(precalculatedMove)) {
							evaluateMove(move, horizon, positionExtension, alpha, beta, false);
							
							if (currentDepth > closeToDepth)
								return;
							else
								closeToDepth = Integer.MAX_VALUE;
							
							if (updateCurrentRecordAfterEvaluation(move, horizon, currentRecord, nextRecord))
								break;
						}
						
						currentRecord.moveListEnd--;
					}
					
					// Rest
					final int idleEngineCount = idleChildEngineList.size();
					final int runningEngineCount = runningChildEngineList.size();
					
					if (horizon > MIN_PARALLEL_HORIZON && idleEngineCount > 0 && runningEngineCount == 0)
						evaluateRestOfMovesParallel(horizon, currentRecord,	positionExtension, nextRecord, precalculatedMove);
					else
						evaluateRestOfMovesSerial(horizon, currentRecord, positionExtension, nextRecord, precalculatedMove);
				}
				
				checkMate(onTurn, horizon, currentRecord, mateEvaluation, currentRecord.attackCalculator);
			}
			
			updateHashRecord(currentRecord, horizon);
		}
		else
			currentRecord.evaluation.setEvaluation(Evaluation.MAX);
	}
	
	public boolean evaluateRestOfMovesSerial(final int horizon, final NodeRecord currentRecord, final int positionExtension, final NodeRecord nextRecord, final Move precalculatedMove) {
		while (currentRecord.moveListEnd > currentRecord.moveListBegin) {
			final Move move = new Move();
			moveStack.getMove(currentRecord.moveListEnd - 1, move);
			
			if (!move.equals(precalculatedMove)) {
				final int alpha = currentRecord.evaluation.getAlpha();
				final int beta = currentRecord.evaluation.getBeta();
						
				evaluateMove(move, horizon, positionExtension, alpha, alpha + 1, false);
				
				final int childEvaluation = -nextRecord.evaluation.getEvaluation();
				
				if (childEvaluation > alpha)
					evaluateMove(move, horizon, positionExtension, alpha, beta, false);

				if (currentDepth > closeToDepth)
					return true;
				else {
					closeToDepth = Integer.MAX_VALUE;
				
					if (updateCurrentRecordAfterEvaluation(move, horizon, currentRecord, nextRecord))
						break;
				}
			}
			
			currentRecord.moveListEnd--;
		}
				
		return false;
	}
		
	public boolean evaluateRestOfMovesParallel(final int horizon, final NodeRecord currentRecord, final int positionExtension, final NodeRecord nextRecord, final Move precalculatedMove) {
		final List<Move> movesToCheck = getRemainingMoveList(currentRecord,precalculatedMove);
		
		subTaskList.clear();
		
		final List<Callable<Throwable>> evaluatorList = new ArrayList<>();
		final Holder<Boolean> betaCutoff = new Holder<>(false);
		
		for (final Move move: movesToCheck) {
			final Callable<Throwable> moveEvaluator = new Callable<Throwable>() {
				@Override
				public Throwable call() throws Exception {
					final SearchTask closedWindowSubTask;
					ISearchEngine engine = null;
					
					try {
						final int origAlpha;
						final int beta;
						
						// Evaluate with closed window
						synchronized (monitor) {
							if (betaCutoff.getValue())
								return null;
							
							origAlpha = currentRecord.evaluation.getAlpha();
							beta = currentRecord.evaluation.getBeta();
							
							evaluateMove(move, horizon, positionExtension, origAlpha, origAlpha + 1, true);
							
							closedWindowSubTask = getSingleSubTask();
							engine = idleChildEngineList.remove(0);
							runningChildEngineList.add(engine);
						}
						
						final SearchResult closedWindowResult = engine.search(closedWindowSubTask);						
						final int closedWindowChildEvaluation = -closedWindowResult.getNodeEvaluation().getEvaluation();
						
						// Recalculate with open window
						final SearchResult recalculateResult;
						final SearchResult totalResult;
						
						if (closedWindowChildEvaluation > origAlpha) {
							final SearchTask recalculateSubTask;
							
							synchronized (monitor) {
								if (betaCutoff.getValue())
									return null;
								
								final int newAlpha = currentRecord.evaluation.getAlpha();
								evaluateMove(move, horizon, positionExtension, newAlpha, beta, true);
								
								recalculateSubTask = getSingleSubTask();
							}
							
							recalculateResult = engine.search(recalculateSubTask);
							totalResult = recalculateResult;
						}
						else {
							recalculateResult = null;
							totalResult = closedWindowResult;
						}

						synchronized (monitor) {
							if (updateCurrentRecordAfterEvaluation(move, horizon, currentRecord, totalResult))
								betaCutoff.setValue(Boolean.TRUE);
							
							nodeCount += closedWindowResult.getNodeCount();
							
							if (recalculateResult != null)
								nodeCount += recalculateResult.getNodeCount();
							
							final int childEvaluation = -totalResult.getNodeEvaluation().getEvaluation();
							
							for (ISearchEngine siblingEngine: runningChildEngineList) {
								if (siblingEngine != engine)
									siblingEngine.updateTaskBoundaries(childEvaluation, Evaluation.MAX);
							}
						}

						return null;
					}
					finally {
						if (engine != null) {
							synchronized (monitor) {
								idleChildEngineList.add(engine);
								runningChildEngineList.remove(engine);
							}
						}
					}
				}
			};
			
			evaluatorList.add(moveEvaluator);			
		}

		invokeCallableList(evaluatorList);
		
		return betaCutoff.getValue();
	}

	private List<Move> getRemainingMoveList(final NodeRecord currentRecord, final Move precalculatedMove) {
		final List<Move> movesToCheck = new ArrayList<>();
		
		while (currentRecord.moveListEnd > currentRecord.moveListBegin) {
			final Move move = new Move();
			moveStack.getMove(currentRecord.moveListEnd - 1, move);
			
			if (!move.equals(precalculatedMove))
				movesToCheck.add(move);
			
			currentRecord.moveListEnd--;
		}
		return movesToCheck;
	}

	public void invokeCallableList(final List<Callable<Throwable>> evaluatorList) {
		try {
			parallel.invokeAll(evaluatorList);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private boolean getIsCheck() {
		final NodeRecord currentRecord = nodeStack[currentDepth];
		
		if (!currentRecord.checkCalculated) {
			currentRecord.isCheck = currentPosition.isCheck();
			currentRecord.checkCalculated = true;
		}
		
		return currentRecord.isCheck;
	}

	private static boolean isLoseMateSearch(final int beta) {
		return beta < -Evaluation.MATE_MIN && beta >= -Evaluation.MATE_ZERO_DEPTH;
	}

	private static boolean isWinMateSearch(final int alpha) {
		return alpha > Evaluation.MATE_MIN && alpha <= Evaluation.MATE_ZERO_DEPTH;
	}

	private boolean checkFiniteEvaluation(final int horizon, final NodeRecord currentRecord, final int initialAlpha, final int initialBeta) {
		if (currentDepth + depthAdvance > 0 && finiteEvaluator.evaluate(currentPosition, currentDepth, horizon, initialAlpha, initialBeta)) {
			currentRecord.evaluation.setEvaluation(finiteEvaluator.getEvaluation());
			
			return true;
		}
		
		return false;
	}

	/**
	 * Checks if there is mate in the position.
	 * At zero horizon method checks legal moves itself, otherwise it uses number of moves
	 * stored in current node record.
	 * @param horizon horizon
	 * @param currentRecord current node record
	 * @param mateEvaluation evaluation of the mate
	 * @param isCheck if there is check in the position
	 * @return if there is a mate
	 */
	private boolean checkMate(final int onTurn, final int horizon, final NodeRecord currentRecord, final int mateEvaluation, final AttackCalculator attackCalculator) {
		boolean isMate = false;
		
		if (currentRecord.legalMoveCount == 0) {
			final boolean isCheck = //attackCalculator.isKingAttacked(onTurn);
			getIsCheck();
			
			if (currentRecord.allMovesGenerated) {
				final int evaluation;

				if (isCheck) {
					evaluation = -mateEvaluation;
					isMate = true;
				}
				else
					evaluation = Evaluation.DRAW;

				currentRecord.evaluation.setEvaluation(evaluation);
			}

			// If we are at horizon 0 not all moves was generated so we must
			// check if there really isn't legal move. This is slow so we
			// will detect only mate in case of check.
			if (isCheck && !isMate && attackCalculator.getCanBeMate()) {
				if (!legalMoveFinder.existsLegalMove(currentPosition)) {
					currentRecord.evaluation.setEvaluation(-mateEvaluation);
					
					isMate = true;
				}
			}
		}
		
		return isMate;
	}

	private boolean updateRecordByHash(final int horizon, final NodeRecord currentRecord, final int initialAlpha, final int initialBeta, final HashRecord hashRecord) {
		if (hashTable.getRecord(currentPosition, hashRecord)) {
			if (currentDepth + depthAdvance > 0 && hashRecord.getHorizon() >= horizon) {
				final int hashEvaluation = hashRecord.getNormalizedEvaluation(currentDepth + depthAdvance);
				final int hashType = hashRecord.getType();

				if (hashType == HashRecordType.VALUE || (hashType == HashRecordType.LOWER_BOUND && hashEvaluation > initialBeta) || (hashType == HashRecordType.UPPER_BOUND && hashEvaluation < initialAlpha)) {
					currentRecord.evaluation.setEvaluation(hashEvaluation);
					
					return true;
				}
			}
			
			currentRecord.hashBestCompressedMove = hashRecord.getCompressedBestMove();
		}
		else
			currentRecord.hashBestCompressedMove = Move.NONE_COMPRESSED_MOVE;
		
		return false;
	}

	private void generateAndSortMoves(final NodeRecord currentRecord, final int horizon, final boolean isQuiescenceSearch, final boolean isCheckSearch) {
		currentRecord.moveListBegin = moveStackTop;
		
		final EvaluatedMoveList rootMoveList = task.getRootMoveList();
		final int rootMoveSize = rootMoveList.getSize();
		
		if (currentDepth == 0 && rootMoveSize > 0) {
			moveStack.copyRecords(rootMoveList, 0, 0, rootMoveSize);
			moveStackTop += rootMoveSize;
				
			currentRecord.moveListEnd = moveStackTop;
		}
		else {
			if (isQuiescenceSearch && !isCheckSearch) {
				final int maxCheckSearchDepth = searchSettings.getMaxCheckSearchDepth();
				
				quiescenceLegalMoveGenerator.setGenerateChecks(horizon > -maxCheckSearchDepth);
				quiescenceLegalMoveGenerator.setPosition(currentPosition);
				quiescenceLegalMoveGenerator.generateMoves();
			}
			else {
				pseudoLegalMoveGenerator.setPosition(currentPosition);
				pseudoLegalMoveGenerator.setReduceMovesInCheck(getIsCheck());
				pseudoLegalMoveGenerator.generateMoves();
				
				currentRecord.allMovesGenerated = true;
			}
			
			currentRecord.moveListEnd = moveStackTop;
			
			moveStack.sortMoves (currentRecord.moveListBegin, currentRecord.moveListEnd);
		}
	}

	/**
	 * Evaluates given move.
	 * @param move move to evaluate
	 * @param horizon needed horizon
	 * @param alpha alpha from current view
	 * @param beta alpha from current view
	 * @return true in case of beta cutoff
	 */
	private ISearchResult evaluateMove(final Move move, final int horizon, final int positionExtension, final int alpha, final int beta, final boolean runParallel) {
		final NodeRecord currentRecord = nodeStack[currentDepth];
		final int beginMaterialEvaluation = currentPosition.getMaterialEvaluation();
		
		currentPosition.makeMove(move);
		
		final int moveExtension;
		
		if (horizon >= searchSettings.getMinExtensionHorizon())
			moveExtension = moveExtensionEvaluator.getExtension(currentPosition, move, task.getRootMaterialEvaluation(), beginMaterialEvaluation);
		else
			moveExtension = 0;
		
		final int subAdvancedDepth = currentDepth + depthAdvance + 1;
		final int subMateEvaluation = Evaluation.getMateEvaluation(subAdvancedDepth);
		final int maxExtension = currentRecord.maxExtension;
		final int totalExtension = Math.min(Math.min(positionExtension + moveExtension, ISearchEngine.HORIZON_GRANULARITY), maxExtension);
		
		int subHorizon = horizon + totalExtension - ISearchEngine.HORIZON_GRANULARITY;
		
		if (isWinMateSearch(alpha)) {
			// Alpha is set to mate evaluation - calculate the difference between mate evaluation
			// in sub depth and alpha and this is the maximal horizon to search.
			final int maxHorizon = (subMateEvaluation - alpha) * ISearchEngine.HORIZON_GRANULARITY;
			subHorizon = Math.min(subHorizon, maxHorizon);
		}

		if (isLoseMateSearch(beta)) {
			// Beta is set to negative mate evaluation - calculate the difference between
			// mate evaluation and negative beta and this is the maximal horizon to search.
			final int maxHorizon = (subMateEvaluation + beta) * ISearchEngine.HORIZON_GRANULARITY;
			subHorizon = Math.min(subHorizon, maxHorizon);
		}

		repeatedPositionRegister.pushPosition (currentPosition, move);
		currentRecord.currentMove.assign(move);
		
		final ISearchResult result;
		
		moveStackTop = currentRecord.moveListEnd;

		if (runParallel) {
			final SearchTask task = new SearchTask();
			task.setAlpha(-beta);
			task.setBeta(-alpha);
			task.setRootMaterialEvaluation(this.task.getRootMaterialEvaluation());
			task.getPosition().assign(currentPosition);
			task.setDepthAdvance(subAdvancedDepth);
			task.setHorizon(subHorizon);
			task.setInitialSearch(this.task.isInitialSearch());
			task.setMaxExtension(maxExtension);
			task.setRepeatedPositionRegister(repeatedPositionRegister.copy());
			task.setMove(move);
			
			subTaskList.add(task);
			result = null;
		}
		else {
			final NodeRecord nextRecord = nodeStack[currentDepth + 1];
			nextRecord.openNode(-beta, -alpha);
			nextRecord.maxExtension = maxExtension - totalExtension;

			currentDepth++;
			alphaBeta(subHorizon);
			currentDepth--;
			
			result = nextRecord;
		}
		
		moveStackTop = currentRecord.moveListEnd;
		
		currentRecord.currentMove.clear();
		repeatedPositionRegister.popPosition();
		currentPosition.undoMove(move);
		
		return result;
	}

	private boolean updateCurrentRecordAfterEvaluation(final Move move, final int horizon, final NodeRecord currentRecord, final ISearchResult result) {
		final NodeEvaluation parentEvaluation = result.getNodeEvaluation().getParent(); 
		final int evaluation = parentEvaluation.getEvaluation();
		final boolean isLegalMove = move.getMoveType() != MoveType.NULL && evaluation > Evaluation.MIN;
		boolean betaCutoff = false;
		
		if (isLegalMove) {
			currentRecord.legalMoveCount++;
		}
		
		if (currentRecord.evaluation.update(parentEvaluation)) {
			// Update principal variation
			currentRecord.principalVariation.clear();
			currentRecord.principalVariation.add(move);
			currentRecord.principalVariation.addAll(result.getPrincipalVariation());
			
			// Update alpha and beta
			if (currentRecord.evaluation.isBetaCutoff()) {
				currentRecord.killerMove.assign(move);
				
				if (horizon > 0)
					historyTable[move.getBeginSquare()][move.getTargetSquare()] += horizon * horizon;
				
				betaCutoff = true;
			}
		}
		
		// Send result and update root move list if depth = 0
		if (currentDepth == 0) {
			if (isLegalMove)
				evaluatedMoveList.addRecord(move, evaluation);
			
			for (ISearchEngineHandler handler: handlerRegistrar.getHandlers()) {
				final SearchResult partialResult = getResult(horizon);
				
				handler.onResultUpdate(partialResult);
			}
		}
		
		return betaCutoff;
	}

	private SearchResult searchOneTask() {
		// Initialize
		final MoveList principalVariation = task.getPrincipalVariation();
		currentPosition.assign(task.getPosition());
		
		nodeStack[0].openNode(task.getAlpha(), task.getBeta());
		nodeStack[0].maxExtension = task.getMaxExtension();
		
		currentDepth = 0;
		moveStackTop = 0;
		depthAdvance = task.getDepthAdvance();
		finiteEvaluator.setDepthAdvance(depthAdvance);
		evaluatedMoveList.clear();

		for (int i = 0; i < maxTotalDepth; i++)
			nodeStack[i].killerMove.clear();

		for (int i = 0; i < principalVariation.getSize(); i++) {
			principalVariation.assignToMove(i, nodeStack[i].principalMove);
		}

		for (int i = principalVariation.getSize(); i < maxTotalDepth; i++)
			nodeStack[i].principalMove.clear();

		nodeCount = 0;

		if (task.isInitialSearch()) {
			for (int i = 0; i < Square.LAST; i++) {
				Arrays.fill(historyTable[i], 0);
			}
		}

		// Do the search
		currentDepth = 0;
		closeToDepth = Integer.MAX_VALUE;
		boolean terminated = false;
		
		try {
			alphaBeta(task.getHorizon());
			
			if (currentDepth != 0)
				throw new RuntimeException("Corrupted depth");
		}
		catch (SearchTerminatedException ex) {
			terminated = true;
		}
		
		final SearchResult result = getResult(task.getHorizon());
		result.setSearchTerminated(terminated);
		
		return result;
	}

	private void updateBoundariesInNodes() {
		int taskAlpha = task.getAlpha();
		int taskBeta = task.getBeta();
		
		for (int depth = 0; depth <= currentDepth; depth++) {
			final NodeRecord nodeRecord = nodeStack[depth];
			
			if (nodeRecord.evaluation.clipBoundaries(taskAlpha, taskBeta)) {
				final int pomAlpha = taskAlpha;
				taskAlpha = -taskBeta;
				taskBeta = -pomAlpha;
				
				closeToDepth = depth;
			}
			else
				break;
		}
	}

	/**
	 * Clips task boundaries.
	 * @param alpha lower boundary
	 * @param beta upper boundary
	 */
	@Override
	public void updateTaskBoundaries (final int alpha, final int beta) {
		synchronized (monitor) {
			if (task != null) {
				task.setAlpha(Math.max(task.getAlpha(), alpha));
				task.setBeta(Math.min(task.getBeta(), beta));
			}
		}
	}

	/**
	 * Sets maximal total depth of the search.
	 * Engine must be in STOPPED state.
	 * @param maxTotalDepth maximal total depth of the search
	 */
	@Override
	public void setMaximalDepth(final int maxTotalDepth) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.maxTotalDepth = maxTotalDepth;

			this.nodeStack = new NodeRecord[maxTotalDepth];
			this.moveStack = new MoveStack(maxTotalDepth * PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION);

			for (int i = 0; i < nodeStack.length; i++)
				this.nodeStack[i] = new NodeRecord(maxTotalDepth - i - 1);
		}
	}

	/**
	 * Sets position evaluator.
	 * Engine must be in STOPPED state.
	 * @param evaluator position evaluator
	 */
	@Override
	public void setPositionEvaluator(final IPositionEvaluator evaluator) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.positionEvaluator = evaluator;
		}
	}

	/**
	 * Searches given task and returns results. Changes state from STOPPED
	 * to SEARCHING and when search is finished changes state from SEARCHING
	 * to STOPPED. 
	 * @param task search task
	 */
	@Override
	public SearchResult search(final SearchTask task) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.task = task;
			this.engineState = EngineState.SEARCHING;
		}
		
		try {
			final RepeatedPositionRegister taskRepeatedPositionRegister = task.getRepeatedPositionRegister();
			
			repeatedPositionRegister.clearAnsReserve(taskRepeatedPositionRegister.getSize() + maxTotalDepth);
			repeatedPositionRegister.pushAll(taskRepeatedPositionRegister);
			
			return searchOneTask();
		}
		finally {
			synchronized (monitor) {
				this.task = null;
				this.engineState = EngineState.STOPPED;
			}
		}
	}
	
	/**
	 * Stops the searching.
	 * Method returns immediately and ensures that method search returns as soon
	 * as possible in the future.
	 */
	@Override
	public void stopSearching() {
		synchronized (monitor) {
			if (engineState == EngineState.SEARCHING) {
				this.engineState = EngineState.STOPPING;
				monitor.notifyAll();
				
				if (task != null)
					task.setTerminated(true);
				
				final Set<ISearchEngine> runningEngines = new HashSet<>(runningChildEngineList);
				runningEngines.removeAll(idleChildEngineList);
				
				for (ISearchEngine engine: runningEngines)
					engine.stopSearching();
			}
		}
	}

	/**
	 * Returns result of the search.
	 * 
	 * @return result of the search
	 */
	private SearchResult getResult(final int horizon) {
		final SearchResult result = new SearchResult();

		result.getNodeEvaluation().assign(nodeStack[0].evaluation.copy());
		result.getPrincipalVariation().assign(nodeStack[0].principalVariation);
		result.setNodeCount(nodeCount);
		result.getRootMoveList().assign(evaluatedMoveList);
		result.setHorizon(horizon);

		return result;
	}

	private void updateHashRecord(final NodeRecord currentRecord, final int horizon) {
		final HashRecord record = new HashRecord();
		final NodeEvaluation nodeEvaluation = currentRecord.evaluation;

		record.setEvaluationAndType(nodeEvaluation, currentDepth + depthAdvance);
		record.setHorizon(horizon);

		if (currentRecord.principalVariation.getSize() > 0) {
			record.setCompressedBestMove(currentRecord.principalVariation.get(0).getCompressedMove());
		}
		else {
			record.setCompressedBestMove(Move.NONE_COMPRESSED_MOVE);
		}
		
		hashTable.updateRecord(currentPosition, record);
	}
	
	/**
	 * Returns current state of the engine.
	 * 
	 * @return engine state
	 */
	public EngineState getEngineState() {
		synchronized (monitor) {
			return engineState;
		}
	}

	/**
	 * Returns number of searched nodes.
	 * 
	 * @return number of searched nodes
	 */
	public long getNodeCount() {
		synchronized (monitor) {
			return nodeCount;
		}
	}

	/**
	 * Sets hash table for the manager. Engine must be in STOPPED state.
	 * 
	 * @param table hash table
	 */
	public void setHashTable(final IHashTable table) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.hashTable = table;
			
			for (ISearchEngine childEngine: runningChildEngineList)
				childEngine.setHashTable(table);
		}
	}

	public SearchSettings getSearchSettings() {
		synchronized (monitor) {
			return searchSettings;
		}
	}

	public void setSearchSettings(final SearchSettings searchSettings) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.searchSettings = searchSettings;
			extensionCalculator.setSearchSettings(searchSettings);
			moveExtensionEvaluator.setSettings(searchSettings);
			
			for (ISearchEngine childEngine: runningChildEngineList)
				childEngine.setSearchSettings(searchSettings);
		}
	}
	
	@Override
	public void setTablebaseEvaluator (final TablebasePositionEvaluator evaluator) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.finiteEvaluator.setTablebaseEvaluator(evaluator);
			
			for (ISearchEngine childEngine: runningChildEngineList)
				childEngine.setTablebaseEvaluator(evaluator);
		}
	}
	
	@Override
	public String toString() {
		synchronized (monitor) {
			final StringWriter result = new StringWriter();
			final PrintWriter writer = new PrintWriter(result);
			
			for (int i = 0; i <= currentDepth; i++) {
				final NodeRecord record = nodeStack[i];
				
				final Move move = new Move();
				moveStack.getMove(record.moveListEnd - 1, move);
				
				writer.println("Current move: " + move);
				writer.println("Evaluation: " + record.evaluation);
				writer.println();
			}
			
			writer.flush();
			
			return result.toString();
		}
	}
	
	/**
	 * Returns registrar for search engine handlers.
	 * @return registrar
	 */
	public IHandlerRegistrar<ISearchEngineHandler> getHandlerRegistrar() {
		return handlerRegistrar;
	}

	private SearchTask getSingleSubTask() {
		if (subTaskList.size() != 1)
			throw new RuntimeException("Corrupted subtask list");
		
		return subTaskList.remove(0);
	}

}
