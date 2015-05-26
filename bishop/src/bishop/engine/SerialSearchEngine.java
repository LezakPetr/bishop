package bishop.engine;

import java.util.Arrays;

import utils.Logger;
import bishop.base.BitBoard;
import bishop.base.Color;
import bishop.base.HandlerRegistrarImpl;
import bishop.base.IHandlerRegistrar;
import bishop.base.IMoveGenerator;
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

	private static class NodeRecord {
		public final Move currentMove;
		public int moveListBegin;
		public int moveListEnd;
		public final MoveList principalVariation;
		public final NodeEvaluation evaluation;
		public final Move killerMove;
		public final Move principalMove;
		public int hashBestCompressedMove;
		public int legalMoveCount;
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
			this.checkCalculated = false;
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
	
	// Settings
	private int maxTotalDepth;
	private SearchSettings searchSettings;
	private HandlerRegistrarImpl<ISearchEngineHandler> handlerRegistrar;
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

	// Synchronization
	private EngineState engineState;
	private Thread thread;
	private final Object monitor;

	// Supplementary objects
	private final QuiescencePseudoLegalMoveGenerator quiescenceLegalMoveGenerator;
	private final IMoveGenerator pseudoLegalMoveGenerator;
	private final LegalMoveFinder legalMoveFinder;
	private final MoveWalker moveWalker;
	private final int[][] historyTable;
	private final FinitePositionEvaluator finiteEvaluator;
	private final SearchExtensionCalculator extensionCalculator;
	private final MoveExtensionEvaluator moveExtensionEvaluator;
	private SearchTask task;
	private IPositionEvaluator positionEvaluator;
	
	public SerialSearchEngine() {
		moveWalker = new MoveWalker();

		quiescenceLegalMoveGenerator = new QuiescencePseudoLegalMoveGenerator();
		quiescenceLegalMoveGenerator.setWalker(moveWalker);

		pseudoLegalMoveGenerator = new PseudoLegalMoveGenerator();
		pseudoLegalMoveGenerator.setWalker(moveWalker);
		
		legalMoveFinder = new LegalMoveFinder();

		engineState = EngineState.STOPPED;
		handlerRegistrar = new HandlerRegistrarImpl<ISearchEngineHandler>();
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

	/**
	 * Starts the engine. Changes state from STOPPED to WAITING.
	 */
	public void start() {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			handlerRegistrar.setChangesEnabled(false);

			thread = new Thread(new Runnable() {
				public void run() {
					searchLoop();
				}
			},
			"SerialSearchEngine thread");
			
			thread.setDaemon(true);
			thread.start();
			engineState = EngineState.WAITING;
		}
	}

	/**
	 * Stops the engine. Changes state from WAITING or SEARCHING to STOPPING and
	 * later to STOPPED.
	 */
	public void stop() {
		synchronized (monitor) {
			checkEngineState(EngineState.SEARCHING, EngineState.WAITING);

			engineState = EngineState.STOPPING;
			monitor.notifyAll();
		}

		Utils.joinThread(thread);
		thread = null;

		synchronized (monitor) {
			task = null;
			handlerRegistrar.setChangesEnabled(true);
			engineState = EngineState.STOPPED;
		}
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
			
			final HashRecord hashRecord = new HashRecord();
			
			if (updateRecordByHash(horizon, currentRecord, initialAlpha, initialBeta, hashRecord))
				return;
			
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
			
			if (isQuiescenceSearch || isMaxDepth) {
				currentRecord.evaluation.setEvaluation(positionEvaluation);
				
				nodeCount++;

				if (currentRecord.evaluation.updateBoundaries (positionEvaluation)) {
					currentRecord.evaluation.setAlpha(positionEvaluation);

					return;
				}
			}

			final int maxQuiescenceDepth = searchSettings.getMaxQuiescenceDepth();
			
			if (!isMaxDepth && horizon > -maxQuiescenceDepth && (!isQuiescenceSearch || !mateRequired)) {
				final NodeRecord nextRecord = nodeStack[currentDepth + 1];
				
				currentRecord.moveListBegin = moveStackTop;
				currentRecord.moveListEnd = moveStackTop;
				
				// Null move heuristic
				if (isNullSearchPossible()) {
					int nullHorizon = horizon - searchSettings.getNullMoveReduction();
					final Move move = new Move();
					move.createNull(currentPosition.getCastlingRights().getIndex(), currentPosition.getEpFile());
					
					evaluateMove(move, nullHorizon, 0, initialBeta, initialBeta + 1);
					
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
					
					evaluateMove(precalculatedMove, horizon, positionExtension, alpha, beta);
					
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
					generateAndSortMoves(currentRecord, horizon, isQuiescenceSearch);
					
					while (currentRecord.moveListEnd > currentRecord.moveListBegin) {
						final Move move = new Move();
						moveStack.getMove(currentRecord.moveListEnd - 1, move);
						
						if (!move.equals(precalculatedMove)) {
							final int alpha = currentRecord.evaluation.getAlpha();
							final int beta = currentRecord.evaluation.getBeta();
							
							if (currentRecord.legalMoveCount == 0 || beta - alpha == 1) {
								evaluateMove(move, horizon, positionExtension, alpha, beta);
							}
							else {
								evaluateMove(move, horizon, positionExtension, alpha, alpha + 1);
								
								final int childEvaluation = -nextRecord.evaluation.getEvaluation();
								
								if (childEvaluation > alpha)
									evaluateMove(move, horizon, positionExtension, alpha, beta);
							}
							
							if (currentDepth > closeToDepth)
								return;
							else
								closeToDepth = Integer.MAX_VALUE;
							
							if (updateCurrentRecordAfterEvaluation(move, horizon, currentRecord, nextRecord))
								break;
						}
						
						currentRecord.moveListEnd--;
					}
				}
				
				checkMate(onTurn, horizon, currentRecord, mateEvaluation, currentRecord.attackCalculator);
			}
			
			updateHashRecord(currentRecord, horizon);
		}
		else
			currentRecord.evaluation.setEvaluation(Evaluation.MAX);
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
		if (finiteEvaluator.evaluate(currentPosition, currentDepth, horizon, initialAlpha, initialBeta)) {
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
			final boolean isCheck = attackCalculator.isKingAttacked(onTurn);
			
			if (horizon >= ISearchEngine.HORIZON_GRANULARITY) {
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
			if (hashRecord.getHorizon() >= horizon) {
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

	private void generateAndSortMoves(final NodeRecord currentRecord, final int horizon, final boolean isQuiescenceSearch) {
		currentRecord.moveListBegin = moveStackTop;
		
		if (isQuiescenceSearch) {
			quiescenceLegalMoveGenerator.setGenerateChecks(horizon >= 0);
			quiescenceLegalMoveGenerator.setPosition(currentPosition);
			quiescenceLegalMoveGenerator.generateMoves();						
		}
		else {
			pseudoLegalMoveGenerator.setPosition(currentPosition);
			pseudoLegalMoveGenerator.generateMoves();
		}
		
		currentRecord.moveListEnd = moveStackTop;
		
		moveStack.sortMoves (currentRecord.moveListBegin, currentRecord.moveListEnd);
	}

	/**
	 * Evaluates given move.
	 * @param move move to evaluate
	 * @param horizon needed horizon
	 * @param alpha alpha from current view
	 * @param beta alpha from current view
	 * @return true in case of beta cutoff
	 */
	private void evaluateMove(final Move move, final int horizon, final int positionExtension, final int alpha, final int beta) {
		final NodeRecord currentRecord = nodeStack[currentDepth];
		final NodeRecord nextRecord = nodeStack[currentDepth + 1];
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

		nextRecord.openNode(-beta, -alpha);
		nextRecord.maxExtension = maxExtension - totalExtension;
		
		repeatedPositionRegister.pushPosition (currentPosition, move);
		currentRecord.currentMove.assign(move);
		
		moveStackTop = currentRecord.moveListEnd;
		
		currentDepth++;
		alphaBeta(subHorizon);
		currentDepth--;
		
		currentRecord.currentMove.clear();
		repeatedPositionRegister.popPosition();
		currentPosition.undoMove(move);
	}

	private boolean updateCurrentRecordAfterEvaluation(final Move move, final int horizon, final NodeRecord currentRecord, final NodeRecord nextRecord) {
		final NodeEvaluation parentEvaluation = nextRecord.evaluation.getParent(); 
		final int evaluation = parentEvaluation.getEvaluation();
		
		if (move.getMoveType() != MoveType.NULL && evaluation > Evaluation.MIN) {
			currentRecord.legalMoveCount++;
		}
		
		if (currentRecord.evaluation.update(parentEvaluation)) {
			// Update principal variation
			currentRecord.principalVariation.clear();
			currentRecord.principalVariation.add(move);
			currentRecord.principalVariation.addAll(nextRecord.principalVariation);

			// Update alpha and beta
			if (currentRecord.evaluation.isBetaCutoff()) {
				currentRecord.killerMove.assign(move);
				
				if (horizon > 0)
					historyTable[move.getBeginSquare()][move.getTargetSquare()] += horizon * horizon;
				
				return true;
			}
		}
		
		return false;
	}

	private void searchOneTask() {
		final SearchResult result;
		
		// Initialize
		final MoveList principalVariation = task.getPrincipalVariation();
		currentPosition.assign(task.getPosition());

		nodeStack[0].openNode(task.getAlpha(), task.getBeta());
		nodeStack[0].maxExtension = task.getMaxExtension();
		
		currentDepth = 0;
		moveStackTop = 0;
		depthAdvance = task.getDepthAdvance();
		finiteEvaluator.setDepthAdvance(depthAdvance);

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
		
		result = getResult();
		result.setSearchTerminated(terminated);
		
		final SearchTask prevTask;
		
		synchronized (monitor) {
			prevTask = task;
			task = null;
			
			monitor.notifyAll();
		}
		
		for (ISearchEngineHandler handler : handlerRegistrar.getHandlers()) {
			handler.onSearchComplete(this, prevTask, result);
		}
	}

	private void searchLoop() {
		while (true) {
			boolean search = false;
			boolean wait = false;

			synchronized (monitor) {
				switch (engineState) {
				case SEARCHING:
					if (task != null) {
						search = true;
					} else {
						wait = true;
					}

					break;

				case TERMINATING:
					// Notify that search was finished
					if (task != null) {
						task = null;
						monitor.notifyAll();
					}

					break;

				case STOPPING:
				case STOPPED:
					return;

				default:
					wait = true;
					break;
				}

				if (wait) {
					try {
						monitor.wait();
					}
					catch (InterruptedException ex) {
						Logger.logException(ex);
					}
				}
			}

			if (search)
				searchOneTask();
		}
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
	 * Engine must be in SEARCHING state.
	 * @param alpha lower boundary
	 * @param beta upper boundary
	 */
	public void updateTaskBoundaries (final int alpha, final int beta) {
		synchronized (monitor) {
			checkEngineState(EngineState.SEARCHING);

			if (task != null) {
				task.setAlpha(Math.max(task.getAlpha(), alpha));
				task.setBeta(Math.min(task.getBeta(), beta));
			}
		}
	}
	
	/**
	 * Terminates the task.
	 * Engine must be in SEARCHING state.
	 */
	public void terminateTask() {
		synchronized (monitor) {
			checkEngineState(EngineState.SEARCHING);

			if (task != null) {
				task.setTerminated (true);
			}
		}
	}

	/**
	 * Sets maximal total depth of the search. Engine must be in STOPPED or
	 * WAITING state.
	 * 
	 * @param maxTotalDepth maximal total depth of the search
	 */
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
	 * Sets position evaluator. Engine must be in STOPPED or WAITING state.
	 * @param evaluator position evaluator
	 */
	public void setPositionEvaluator(final IPositionEvaluator evaluator) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.positionEvaluator = evaluator;
		}
	}

	/**
	 * Sets task for searching. Engine must be in SEARCHING state.
	 * @param task search task
	 */
	public void startSearching(final SearchTask task) {
		synchronized (monitor) {
			checkEngineState(EngineState.WAITING);

			if (this.task != null)
				throw new RuntimeException("Engine is searching");

			this.task = task;
			this.engineState = EngineState.SEARCHING;
			
			final RepeatedPositionRegister taskRepeatedPositionRegister = task.getRepeatedPositionRegister();
			
			repeatedPositionRegister.clearAnsReserve(taskRepeatedPositionRegister.getSize() + maxTotalDepth);
			repeatedPositionRegister.pushAll(taskRepeatedPositionRegister);

			monitor.notifyAll();
		}
	}
	
	/**
	 * Stops the search. Engine must be in SEARCHING state.
	 */
	public void stopSearching() {
		synchronized (monitor) {
			checkEngineState(EngineState.SEARCHING);

			this.engineState = EngineState.TERMINATING;
			monitor.notifyAll();

			while (task != null) {
				try {
					monitor.wait();
				}
				catch (InterruptedException ex) {
					Logger.logException(ex);
				}
			}

			this.engineState = EngineState.WAITING;
			monitor.notifyAll();
		}
	}

	/**
	 * Checks if engine has some task.
	 * 
	 * @return true if engine has some task, false if not
	 */
	public boolean hasTask() {
		synchronized (monitor) {
			return task != null;
		}
	}

	/**
	 * Returns handler registrar of this engine. Modification is enabled just in
	 * STOPPED state.
	 * 
	 * @return registrar
	 */
	public IHandlerRegistrar<ISearchEngineHandler> getHandlerRegistrar() {
		return handlerRegistrar;
	}

	/**
	 * Returns result of the search. Engine must be in WAITING state.
	 * 
	 * @return result of the search
	 */
	private SearchResult getResult() {
		final SearchResult result = new SearchResult();

		result.getNodeEvaluation().assign(nodeStack[0].evaluation.copy());
		result.getPrincipalVariation().assign(nodeStack[0].principalVariation);
		result.setNodeCount(nodeCount);

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
		}
	}
	
	@Override
	public void setTablebaseEvaluator (final TablebasePositionEvaluator evaluator) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.finiteEvaluator.setTablebaseEvaluator(evaluator);
		}
	}

}
