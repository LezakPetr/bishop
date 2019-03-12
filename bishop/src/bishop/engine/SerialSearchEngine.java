package bishop.engine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.IntStream;

import bishop.base.*;
import utils.Logger;
import utils.RatioCalculator;

public final class SerialSearchEngine implements ISearchEngine {

	public static final int HORIZON_STEP_WITHOUT_EXTENSION = 2;
	private static final int EXTENSION_HASH_MASK = (1 << SearchSettings.EXTENSION_FRACTION_BITS) - 1;

	private static final int MAX_POSITIONAL_EVALUATION = 3 * PieceTypeEvaluations.PAWN_EVALUATION;

	public class NodeRecord implements ISearchResult {
		private class MoveWalker implements IMoveWalker {
			private final HashRecord estimateHashRecord = new HashRecord();

			public boolean processMove(final Move move) {
				int estimate;

				if (move.equals(hashBestMove))
					estimate = HASH_BEST_MOVE_ESTIMATE;
				else {
					estimate = moveEstimator.getMoveEstimate(NodeRecord.this, currentPosition.getOnTurn(), move);

					// Sort moves by hash table in depth 0.
					if (depth == 0) {
						currentPosition.makeMove(move);

						if (evaluationHashTable.getRecord(currentPosition, -1, estimateHashRecord))
							estimate = -estimateHashRecord.getEvaluation();

						currentPosition.undoMove(move);
					}
				}

				moveStack.setRecord(moveStackTop, move, estimate);
				moveStackTop++;

				return true;
			}
		};

		private final MoveWalker moveWalker;
		private final QuiescencePseudoLegalMoveGenerator quiescenceLegalMoveGenerator;
		private final PseudoLegalMoveGenerator pseudoLegalMoveGenerator;
		private final int depth;
		private final NodeRecord nextRecord;
		private NodeRecord previousRecord;
		private final Move currentMove = new Move();
		private int moveListBegin;
		private int moveListEnd;
		private final MoveList principalVariation;
		private final Move killerMove = new Move();
		private final Move originalKillerMove = new Move();
		private final Move hashBestMove = new Move();
		private final Move firstLegalMove = new Move();
		private boolean allMovesGenerated;
		private boolean isQuiescenceSearch;
		private int legalMoveCount;
		private int bestLegalMoveIndex;
		private final MobilityCalculator mobilityCalculator = new MobilityCalculator();
		private int evaluation;
		private int alpha;
		private int beta;

		// Precreated objects to prevent reallocation
		private final HashRecord hashRecord = new HashRecord();
		private final Move nullMove = new Move();
		private final Move precalculatedMove = new Move();
		private final Move precreatedCurrentMove = new Move();

		public NodeRecord(final int depth, final int maxPrincipalDepth, final NodeRecord nextRecord) {
			this.depth = depth;
			this.nextRecord = nextRecord;
			this.principalVariation = new MoveList(maxPrincipalDepth);

			moveWalker = new MoveWalker();

			quiescenceLegalMoveGenerator = new QuiescencePseudoLegalMoveGenerator();
			quiescenceLegalMoveGenerator.setWalker(moveWalker);

			pseudoLegalMoveGenerator = new PseudoLegalMoveGenerator();
			pseudoLegalMoveGenerator.setWalker(moveWalker);
		}

		public Move getKillerMove() {
			return killerMove;
		}

		public Move getOriginalKillerMove() {
			return originalKillerMove;
		}

		public Move getFirstLegalMove() {
			return firstLegalMove;
		}

		public void setPreviousRecord(final NodeRecord previousRecord) {
			this.previousRecord = previousRecord;
		}

		public NodeRecord getPreviousRecord() {
			return previousRecord;
		}

		public int getDepth() {
			return depth;
		}

		public void openNode(final int alpha, final int beta) {
			this.evaluation = Evaluation.MIN;
			this.alpha = alpha;
			this.beta = beta;
			this.firstLegalMove.clear();
			this.allMovesGenerated = false;
			this.legalMoveCount = 0;
			this.bestLegalMoveIndex = -1;
		}

		public void clear() {
			evaluation = Evaluation.MIN;
			alpha = Evaluation.MIN;
			beta = Evaluation.MAX;
			currentMove.clear();
			moveListBegin = 0;
			moveListEnd = 0;
			principalVariation.clear();
			killerMove.clear();
			hashBestMove.clear();
			firstLegalMove.clear();
			allMovesGenerated = false;
			isQuiescenceSearch = false;
			legalMoveCount = 0;
			bestLegalMoveIndex = 0;
			hashRecord.clear();
			nullMove.clear();
			precalculatedMove.clear();
			precreatedCurrentMove.clear();
		}

		public int getEvaluation() {
			return evaluation;
		}

		public MoveList getPrincipalVariation() {
			return principalVariation;
		}

		private void alphaBeta (final int horizon) {
			receiveUpdates();

			final int onTurn = currentPosition.getOnTurn();
			mobilityCalculator.calculate(currentPosition, (depth > 0) ? nodeStack[depth - 1].mobilityCalculator : null);

			final int oppositeColor = Color.getOppositeColor(onTurn);
			final boolean isLegalPosition = !mobilityCalculator.isSquareAttacked(onTurn, currentPosition.getKingPosition(oppositeColor));
			principalVariation.clear();

			if (isLegalPosition)
				alphaBetaInLegalPosition(horizon);
			else
				evaluation = Evaluation.MAX;
		}

		private void alphaBetaInLegalPosition(final int horizon) {
			final int onTurn = currentPosition.getOnTurn();
			final int oppositeColor = Color.getOppositeColor(onTurn);

			if (checkFiniteEvaluation(horizon))
				return;

			moveListBegin = moveStackTop;

			// Try to find position in hash table
			if (updateRecordByHash(horizon, hashRecord))
				return;

			final int reducedHorizon = shouldReduceHorizon(horizon) ? 0 : horizon;
			isQuiescenceSearch = (reducedHorizon <= 0);

			final int ownKingSquare = currentPosition.getKingPosition(onTurn);
			final boolean isCheck = mobilityCalculator.isSquareAttacked(oppositeColor, ownKingSquare);

			originalKillerMove.assign(killerMove);

			try {
				// Evaluate position
				final int positionEvaluation = getPositionEvaluation(onTurn);
				final int maxCheckSearchDepth = searchSettings.getMaxCheckSearchDepth();

				final boolean isCheckSearch = isQuiescenceSearch && reducedHorizon > -maxCheckSearchDepth && isCheck;

				final int positionExtension;

				if (reducedHorizon >= searchSettings.getMinExtensionHorizon())
					positionExtension = extensionCalculator.getExtension(currentPosition, isCheck, hashRecord, horizon);
				else
					positionExtension = 0;

				final boolean isMaxDepth = (depth >= maxTotalDepth - 1);

				// Use position evaluation as initial evaluation
				if ((isQuiescenceSearch && !isCheckSearch) || isMaxDepth) {
					evaluation = positionEvaluation;
					alpha = Math.max(alpha, evaluation);

					nodeCount++;

					if (evaluation > beta) {
						final int mateEvaluation = Evaluation.getMateEvaluation(depth);
						checkMateAndStalemate(isCheck, mateEvaluation);

						return;
					}
				}

				final int maxQuiescenceDepth = searchSettings.getMaxQuiescenceDepth();

				if (!isMaxDepth && reducedHorizon > -maxQuiescenceDepth) {
					moveListBegin = moveStackTop;
					moveListEnd = moveStackTop;

					// Null move heuristic
					if (!isQuiescenceSearch && isNullSearchPossible(isCheck)) {
						final int nullReduction = Math.min(searchSettings.getNullMoveReduction(), reducedHorizon / 2);

						if (nullReduction > 0) {
							int nullHorizon = reducedHorizon - nullReduction;
							nullMove.createNull(currentPosition.getCastlingRights().getIndex(), currentPosition.getEpFile());

							final int beginMaterialEvaluation = currentPosition.getMaterialEvaluation();
							currentPosition.makeMove(nullMove);

							if (beta < Evaluation.MIN) {
								evaluateMadeMove (nullMove, nullHorizon, 0, beta, beta, beginMaterialEvaluation);
								final int childEvaluation = -nextRecord.evaluation;

								if (childEvaluation < beta) {
									evaluateMadeMove(nullMove, nullHorizon, 0, alpha, childEvaluation, beginMaterialEvaluation);
									final int updatedChildEvaluation = -nextRecord.evaluation;

									assert (updatedChildEvaluation <= childEvaluation);
								}
							}
							else
								evaluateMadeMove(nullMove, nullHorizon, 0, alpha, beta, beginMaterialEvaluation);

							currentPosition.undoMove(nullMove);

							evaluation = Math.max(evaluation, -nextRecord.evaluation);
							alpha = Math.max(alpha, evaluation);

							if (evaluation > beta)
								return;
						}
					}

					// Try P-var move first
					final boolean precalculatedMoveFound;
					boolean precalculatedBetaCutoff = false;

					if (isQuiescenceSearch) {
						precalculatedMoveFound = false;
						precalculatedMove.clear();
					}
					else {
						if (hashBestMove.getMoveType() != MoveType.INVALID) {
							precalculatedMoveFound = true;
							precalculatedMove.assign(hashBestMove);
						}
						else
							precalculatedMoveFound = false;
					}

					if (precalculatedMoveFound) {
						evaluateMove(precalculatedMove, reducedHorizon, positionExtension, alpha, beta);

						if (updateCurrentRecordAfterEvaluation(precalculatedMove, reducedHorizon, nextRecord)) {
							precalculatedBetaCutoff = true;
						}
					}

					// If precalculated move didn't make beta cutoff try other moves
					if (!precalculatedBetaCutoff) {
						generateAndSortMoves(reducedHorizon, isQuiescenceSearch, isCheckSearch, isCheck);

						// First legal move
						while (moveListEnd > moveListBegin) {
							final Move move = precreatedCurrentMove;
							moveStack.getMove(moveListEnd - 1, move);

							if (!move.equals(precalculatedMove)) {
								final int beginMaterialEvaluation = currentPosition.getMaterialEvaluation();
								currentPosition.makeMove(move);

								if (firstLegalMove.getMoveType() != MoveType.INVALID && alpha != beta && moveStack.getEvaluation(moveListEnd - 1) < searchSettings.getMaxEstimateForZeroWindowSearch()) {
									evaluateMadeMove (move, reducedHorizon, positionExtension, alpha, alpha, beginMaterialEvaluation);
									final int childEvaluation = -nextRecord.evaluation;

									if (childEvaluation > alpha && childEvaluation <= beta) {
										evaluateMadeMove(move, reducedHorizon, positionExtension, childEvaluation, beta, beginMaterialEvaluation);
										final int updatedChildEvaluation = -nextRecord.evaluation;

										assert (updatedChildEvaluation >= childEvaluation);
									}
								}
								else
									evaluateMadeMove(move, reducedHorizon, positionExtension, alpha, beta, beginMaterialEvaluation);

								currentPosition.undoMove(move);

								if (updateCurrentRecordAfterEvaluation(move, reducedHorizon, nextRecord))
									break;
							}

							moveListEnd--;
						}
					}

					if (principalVariation.getSize() > 0)
						moveEstimator.updateMove(this, currentPosition.getOnTurn(), principalVariation.get(0), horizon, true);

					final int mateEvaluation = Evaluation.getMateEvaluation(depth);
					checkMateAndStalemate(isCheck, mateEvaluation);
				}
			}
			finally {
				updateHashRecord(reducedHorizon);
				updateBestMovePerIndexCounts();
			}
		}

		private int getPositionEvaluation(int onTurn) {
			int whitePositionEvaluation = positionEvaluator.evaluateTactical(currentPosition, mobilityCalculator).getEvaluation();

			final int materialEvaluation = currentPosition.getMaterialEvaluation();
			final int materialEvaluationShift = positionEvaluator.getMaterialEvaluationShift();

			whitePositionEvaluation += materialEvaluation >> materialEvaluationShift;

			int relativeEvaluation = Evaluation.getRelative(whitePositionEvaluation, onTurn);

			if (relativeEvaluation + MAX_POSITIONAL_EVALUATION < alpha)
				relativeEvaluation += MAX_POSITIONAL_EVALUATION;
			else if (relativeEvaluation - MAX_POSITIONAL_EVALUATION > beta)
				relativeEvaluation -= MAX_POSITIONAL_EVALUATION;
			else {
				final int positionalEvaluation = positionEvaluator.evaluatePositional().getEvaluation();
				final int boundedPositionalEvaluation = Math.max(Math.min(positionalEvaluation, MAX_POSITIONAL_EVALUATION), -MAX_POSITIONAL_EVALUATION);
				relativeEvaluation += Evaluation.getRelative(boundedPositionalEvaluation, onTurn);
			}

			return fixDrawByRepetitionEvaluation(relativeEvaluation);
		}

		private boolean shouldReduceHorizon(int horizon) {
			return depth >= 2 &&
			       horizon >= 1 &&
			       horizon <= HORIZON_STEP_WITHOUT_EXTENSION &&
			       mobilityCalculator.isStablePosition (currentPosition) &&
			       (currentPosition.getPiecesMask(Color.WHITE, PieceType.PAWN) & BoardConstants.RANK_7_MASK) == 0 &&
			       (currentPosition.getPiecesMask(Color.BLACK, PieceType.PAWN) & BoardConstants.RANK_2_MASK) == 0;
		}

		private boolean isNullSearchPossible(final boolean isCheck) {
			if (isCheck || depth == 0)
				return false;

			final Move lastMove = previousRecord.currentMove;

			if (lastMove.getMoveType() == MoveType.NULL)
				return false;

			// Check position - at least two figures are needed
			final int onTurn = currentPosition.getOnTurn();
			final long figureMask = currentPosition.getColorOccupancy(onTurn) & ~currentPosition.getBothColorPiecesMask(PieceType.PAWN);
			final int figureCount = BitBoard.getSquareCount(figureMask);

			return figureCount >= 3;   // At least two figures + king
		}

		/**
		 * Updates currentRecord by hash table.
		 * @param horizon current horizon
		 * @param hashRecord target hash record
		 * @return true if the position evaluation with enough horizon was obtained
		 */
		private boolean updateRecordByHash(final int horizon, final HashRecord hashRecord) {
			final int compressedBestMove = bestMoveHashTable.getRecord(currentPosition);

			if (compressedBestMove != Move.NONE_COMPRESSED_MOVE)
				hashBestMove.uncompressMove(compressedBestMove, currentPosition);
			else
				hashBestMove.clear();

			if (!readHashRecord(horizon, hashRecord))
				return false;

			if (depth > 0 && hashRecord.getHorizon() == horizon) {
				final int hashEvaluation = hashRecord.getNormalizedEvaluation(depth);
				final int hashType = hashRecord.getType();

				switch (hashType) {
					case HashRecordType.VALUE:
						evaluation = hashEvaluation;
						return true;

					case HashRecordType.LOWER_BOUND:
						if (hashEvaluation > beta) {
							evaluation = hashEvaluation;
							return true;
						}
						else
							alpha = Math.max(alpha, hashEvaluation);

						break;

					case HashRecordType.UPPER_BOUND:
						if (hashEvaluation < alpha) {
							evaluation = hashEvaluation;
							return true;
						}
						else
							beta = Math.min(beta, hashEvaluation);

						break;
				}
			}

			return false;
		}

		private void generateAndSortMoves(final int horizon, final boolean isQuiescenceSearch, final boolean isCheckSearch, final boolean isCheck) {
			moveListBegin = moveStackTop;

			final EvaluatedMoveList rootMoveList = task.getRootMoveList();
			final int rootMoveSize = rootMoveList.getSize();

			if (depth == 0 && rootMoveSize > 0) {
				moveStack.copyRecords(rootMoveList, 0, 0, rootMoveSize);
				moveStackTop += rootMoveSize;

				moveListEnd = moveStackTop;
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
					pseudoLegalMoveGenerator.setReduceMovesInCheck(isCheck);
					pseudoLegalMoveGenerator.generateMoves();

					allMovesGenerated = true;
				}

				moveListEnd = moveStackTop;

				moveStack.sortMoves (moveListBegin, moveListEnd);
			}
		}

		/**
		 * Evaluates given move.
		 * @param move move to evaluate
		 * @param horizon needed horizon
		 * @param alpha alpha from current view
		 * @param beta alpha from current view
		 */
		private void evaluateMove(final Move move, final int horizon, final int positionExtension, final int alpha, final int beta) {
			final int beginMaterialEvaluation = currentPosition.getMaterialEvaluation();

			currentPosition.makeMove(move);
			evaluateMadeMove(move, horizon, positionExtension, alpha, beta, beginMaterialEvaluation);
			currentPosition.undoMove(move);
		}

		private void evaluateMadeMove(final Move move, final int horizon, final int positionExtension, final int alpha, final int beta, final int beginMaterialEvaluation) {
			final int moveExtension;

			if (horizon >= searchSettings.getMinExtensionHorizon())
				moveExtension = moveExtensionEvaluator.getExtension(currentPosition, move, task.getRootMaterialEvaluation(), beginMaterialEvaluation);
			else
				moveExtension = 0;

			final long positionDependentRandomNumber = currentPosition.getHash() & EXTENSION_HASH_MASK;
			final boolean shouldExtend = positionDependentRandomNumber < positionExtension + moveExtension;
			final int totalExtension = shouldExtend ? 1 : 0;
			final int subHorizon = horizon + totalExtension - HORIZON_STEP_WITHOUT_EXTENSION;

			repeatedPositionRegister.pushPosition (currentPosition, move);
			currentMove.assign(move);

			moveStackTop = moveListEnd;

			nextRecord.openNode(-beta, -alpha);
			nextRecord.alphaBeta(subHorizon);

			moveStackTop = moveListEnd;

			currentMove.clear();
			repeatedPositionRegister.popPosition();
		}

		private boolean updateCurrentRecordAfterEvaluation(final Move move, final int horizon, final ISearchResult result) {
			final int parentEvaluation = -result.getEvaluation();
			final boolean isLegalMove = move.getMoveType() != MoveType.NULL && parentEvaluation > Evaluation.MIN;
			boolean betaCutoff = false;

			if (isLegalMove && firstLegalMove.getMoveType() == MoveType.INVALID) {
				firstLegalMove.assign(move);
			}

			if (parentEvaluation > evaluation) {
				if (principalVariation.getSize() > 0)
					moveEstimator.updateMove(this, currentPosition.getOnTurn(), principalVariation.get(0), horizon, false);

				evaluation = parentEvaluation;
				alpha = Math.max(alpha, evaluation);

				// Update principal variation
				principalVariation.clear();

				if (this.evaluation != Evaluation.MIN) {   // Move where king is left attacked is not legal
					principalVariation.add(move);
					principalVariation.addAll(result.getPrincipalVariation());
				}

				// Update alpha and beta
				if (evaluation > beta) {
					killerMove.assign(move);

					betaCutoff = true;
				}

				bestLegalMoveIndex = legalMoveCount;
				legalMoveCount++;
			}

			// Send result and update root move list if depth = 0
			if (depth == 0) {
				if (isLegalMove)
					evaluatedMoveList.addRecord(move, evaluation);

				for (ISearchEngineHandler handler: handlerRegistrar.getHandlers()) {
					final SearchResult partialResult = getResult(horizon);

					handler.onResultUpdate(partialResult);
				}
			}

			return betaCutoff;
		}

		private void updateHashRecord(final int horizon) {
			if (horizon > 0 && !Evaluation.isDrawByRepetition(evaluation)) {
				final HashRecord record = hashRecord;
				record.setEvaluationAndType(evaluation, alpha, beta, depth);
				record.setHorizon(horizon);

				evaluationHashTable.updateRecord(currentPosition, record);

				if (principalVariation.getSize() > 0)
					bestMoveHashTable.updateRecord(currentPosition, horizon, principalVariation.getCompressedMove(0));
			}
		}

		private void updateBestMovePerIndexCounts() {
			if (GlobalSettings.isDebug()) {
				final int index = bestLegalMoveIndex;

				if (index >= 0)
					bestMovePerIndexCounts[index]++;
			}
		}

		/**
		 * Checks if there is mate in the position.
		 * At zero horizon method checks legal moves itself, otherwise it uses number of moves
		 * stored in current node record.
		 * @param isCheck if there is check in the position
		 * @param mateEvaluation evaluation of the mate
		 */
		private void checkMateAndStalemate(final boolean isCheck, final int mateEvaluation) {
			if (firstLegalMove.getMoveType() == MoveType.INVALID) {
				if (allMovesGenerated)
					evaluation = (isCheck) ? -mateEvaluation : Evaluation.DRAW;
				else {
					// If we are at horizon 0 not all moves was generated so we must
					// check if there really isn't legal move. This is slow so we
					// will detect only mate in case of check.
					if (isCheck && mobilityCalculator.canBeMate(currentPosition)) {
						if (!legalMoveFinder.existsLegalMove(currentPosition))
							this.evaluation = -mateEvaluation;
					}
				}
			}
		}

		private boolean checkFiniteEvaluation(final int horizon) {
			if (depth > 0 && finiteEvaluator.evaluate(currentPosition, depth, horizon, alpha, beta)) {
				evaluation = finiteEvaluator.getEvaluation();

				return true;
			}

			return false;
		}

		private boolean readHashRecord(final int horizon, final HashRecord hashRecord) {
			if (horizon <= 0)
				return false;

			final boolean success = readHashRecordImpl(horizon, hashRecord);

			if (GlobalSettings.isDebug())
				hashSuccessRatio.addInvocation(success);

			return success;
		}

		private boolean readHashRecordImpl(final int horizon, final HashRecord hashRecord) {
			return evaluationHashTable.getRecord(currentPosition, horizon, hashRecord);
		}
	}

	private static final int RECEIVE_UPDATES_COUNT = 8192;

	private static final int HASH_BEST_MOVE_ESTIMATE = Integer.MAX_VALUE;
	
	// Settings
	private int maxTotalDepth;
	private SearchSettings searchSettings;
	private IEvaluationHashTable evaluationHashTable;
	private IBestMoveHashTable bestMoveHashTable;

	// Actual task
	private NodeRecord[] nodeStack;
	private MoveStack moveStack;
	private final Position currentPosition;
	private int moveStackTop;
	private long nodeCount;
	private volatile long reportedNodeCount;
	private final RepeatedPositionRegister repeatedPositionRegister;
	private final EvaluatedMoveList evaluatedMoveList;

	// Synchronization
	private EngineState engineState;
	private final Object monitor;
	private int receiveUpdatesCounter;

	// Supplementary objects
	private final LegalMoveFinder legalMoveFinder;
	private final MoveEstimator moveEstimator;
	private final FinitePositionEvaluator finiteEvaluator;
	private final SearchExtensionCalculator extensionCalculator;
	private final MoveExtensionEvaluator moveExtensionEvaluator;
	private SearchTask task;
	private IPositionEvaluator positionEvaluator;
	private final HandlerRegistrarImpl<ISearchEngineHandler> handlerRegistrar;
	private final MateFinder mateFinder;
	
	private static final long[] bestMovePerIndexCounts = new long[PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION];
	
	private static final int MAX_ATTACK = AttackCalculator.MAX_REASONABLE_ATTACK_EVALUATION;

	private static final int WIN_MATE_DEPTH = 1;
	private static final int LOSE_MATE_DEPTH = 1;
	private static final int MAX_MATE_DEPTH = Math.max(WIN_MATE_DEPTH, LOSE_MATE_DEPTH);
	private static final int MAX_MATE_EXTENSION = 4;
	
	private static final RatioCalculator hashSuccessRatio = new RatioCalculator();

	
	public SerialSearchEngine() {
		this.evaluatedMoveList = new EvaluatedMoveList(PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION);
		this.handlerRegistrar = new HandlerRegistrarImpl<>();

		legalMoveFinder = new LegalMoveFinder();

		engineState = EngineState.STOPPED;
		monitor = new Object();
		moveEstimator = new MoveEstimator();
		
		currentPosition = new Position(false);
		repeatedPositionRegister = new RepeatedPositionRegister();
		
		finiteEvaluator = new FinitePositionEvaluator();
		finiteEvaluator.setRepeatedPositionRegister(repeatedPositionRegister);
		
		extensionCalculator = new SearchExtensionCalculator();
		moveExtensionEvaluator = new MoveExtensionEvaluator();
		
		mateFinder = new MateFinder();
		
		setHashTable(new NullEvaluationHashTable(), new NullBestMoveHashTable());

		task = null;
	}
	
	private static RatioCalculator[] createMateStatistics() {
		return IntStream.rangeClosed(0, MAX_ATTACK)
				.mapToObj(i -> new RatioCalculator())
				.toArray(RatioCalculator[]::new);
	}

	/**
	 * Checks if engine is in one of given expected states. If not exception is
	 * thrown. Expects that calling thread owns the monitor.
	 * @param expectedStates expected engine states
	 */
	private void checkEngineState(final EngineState... expectedStates) {
		for (EngineState state : expectedStates) {
			if (state == engineState)
				return;
		}

		throw new RuntimeException("Engine is not in expected state, but in state " + engineState.name());
	}
	
	private void receiveUpdates() {
		receiveUpdatesCounter++;
		
		if (receiveUpdatesCounter >= RECEIVE_UPDATES_COUNT) {
			synchronized (monitor) {
				reportedNodeCount = nodeCount;
				
				if (task.isTerminated()) {
					Logger.logMessage("SerialSearchEngine task termination received");
					throw new SearchTerminatedException();
				}
			}
			
			receiveUpdatesCounter = 0;
		}
	}

	private int fixDrawByRepetitionEvaluation(final int evaluation) {
		return (Evaluation.isDrawByRepetition(evaluation)) ? Evaluation.DRAW : evaluation;
	}

	public static int matePrunning(final int currentDepth, int subHorizon, final int alpha, final int beta) {
		final int subAdvancedDepth = currentDepth + 1;
		final int subMateEvaluation = Evaluation.getMateEvaluation(subAdvancedDepth);
		
		if (Evaluation.isWinMateSearch(alpha)) {
			// Alpha is set to mate evaluation - calculate the difference between mate evaluation
			// in sub depth and alpha and this is the maximal horizon to search.
			final int maxHorizon = HORIZON_STEP_WITHOUT_EXTENSION * (subMateEvaluation - alpha);
			subHorizon = Math.min(subHorizon, maxHorizon);
		}

		if (Evaluation.isLoseMateSearch(beta)) {
			// Beta is set to negative mate evaluation - calculate the difference between
			// mate evaluation and negative beta and this is the maximal horizon to search.
			final int maxHorizon = HORIZON_STEP_WITHOUT_EXTENSION * (subMateEvaluation + beta);
			subHorizon = Math.min(subHorizon, maxHorizon);
		}
		return subHorizon;
	}

	private SearchResult searchOneTask() {
		// Initialize
		final MoveList principalVariation = task.getPrincipalVariation();
		currentPosition.assign(task.getPosition());
		
		nodeStack[0].openNode(task.getAlpha(), task.getBeta());

		moveStackTop = 0;
		evaluatedMoveList.clear();

		for (int i = 0; i < maxTotalDepth; i++)
			nodeStack[i].killerMove.clear();

		nodeCount = 0;
		reportedNodeCount = nodeCount;
		
		// Do the search
		boolean terminated = false;
		
		try {
			nodeStack[0].alphaBeta(task.getHorizon());
		}
		catch (SearchTerminatedException ex) {
			terminated = true;
		}
		
		final SearchResult result = getResult(task.getHorizon());
		result.setSearchTerminated(terminated);

		return result;
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

			NodeRecord nodeRecord = null;

			for (int i = nodeStack.length - 1; i >= 0; i--) {
				this.nodeStack[i] = new NodeRecord(i, maxTotalDepth - i - 1, nodeRecord);
				nodeRecord = nodeStack[i];
			}

			for (int i = 1; i < nodeStack.length; i++) {
				nodeStack[i].setPreviousRecord (nodeStack[i - 1]);
			}
			
			mateFinder.setMaxDepth (MAX_MATE_DEPTH, maxTotalDepth, MAX_MATE_EXTENSION);
		}
	}

	/**
	 * Sets piece type evaluations.
	 * Engine must be in STOPPED state.
	 */
	@Override
	public void setPieceTypeEvaluations(final PieceTypeEvaluations pieceTypeEvaluations) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			moveExtensionEvaluator.setPieceTypeEvaluations(pieceTypeEvaluations);
			finiteEvaluator.setPieceTypeEvaluations(pieceTypeEvaluations);
			currentPosition.setPieceTypeEvaluations(pieceTypeEvaluations);
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
				
				if (GlobalSettings.isDebug())
					printStatistics();
				
				Logger.logMessage("SerialSearchEngine stopSearching");
			}
		}
	}
	
	private void printStatistics() {
		Logger.logMessage("Mate search");

		final double hitRatio = hashSuccessRatio.getRatio();
		
		Logger.logMessage("Hash hitratio = " + hitRatio);

		System.out.println("Best move perindex counts: ");
		
		for (int i = 0; i < bestMovePerIndexCounts.length; i++) {
			if (bestMovePerIndexCounts[i] > 0)
				System.out.println(i + " " + bestMovePerIndexCounts[i]);
		}

		moveEstimator.log();
	}

	/**
	 * Returns result of the search.
	 * 
	 * @return result of the search
	 */
	private SearchResult getResult(final int horizon) {
		reportedNodeCount = nodeCount;

		final SearchResult result = new SearchResult();

		result.setEvaluation(nodeStack[0].evaluation);
		result.getPrincipalVariation().assign(nodeStack[0].principalVariation);
		result.setNodeCount(reportedNodeCount);
		result.getRootMoveList().assign(evaluatedMoveList);
		result.setHorizon(horizon);

		return result;
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
		return reportedNodeCount;
	}

	/**
	 * Sets hash table for the manager. Engine must be in STOPPED state.
	 */
	@Override
	public void setHashTable(final IEvaluationHashTable evaluationHashTable, final IBestMoveHashTable bestMoveHashTable) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.evaluationHashTable = evaluationHashTable;
			this.bestMoveHashTable = bestMoveHashTable;
		}
	}

	@Override
	public void setCombinedPositionEvaluationTable(final CombinedPositionEvaluationTable table) {
		synchronized (monitor) {
			checkEngineState(EngineState.STOPPED);

			this.currentPosition.setCombinedPositionEvaluationTable(table);
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
	
	@Override
	public String toString() {
		synchronized (monitor) {
			final StringWriter result = new StringWriter();
			final PrintWriter writer = new PrintWriter(result);
			
/*			for (int i = 0; i <= currentDepth; i++) {
				final NodeRecord record = nodeStack[i];
				
				final Move move = new Move();
				moveStack.getMove(record.moveListEnd - 1, move);
				
				writer.println("Current move: " + move);
				writer.println("Evaluation: " + record.evaluation);
				writer.println();
			}*/
			
			writer.flush();
			
			return result.toString();
		}
	}

	/**
	 * Clears the engine.
	 * Engine must be in STOPPED state.
	 */
	@Override
	public void clear() {
		checkEngineState(EngineState.STOPPED);

		for (NodeRecord nodeRecord: nodeStack)
			nodeRecord.clear();

		moveEstimator.clear();
	}


	/**
	 * Returns registrar for search engine handlers.
	 * @return registrar
	 */
	public IHandlerRegistrar<ISearchEngineHandler> getHandlerRegistrar() {
		return handlerRegistrar;
	}
}
