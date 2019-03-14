package bishop.engine;

import java.util.Arrays;

import bishop.base.Color;
import bishop.base.MateChecker;
import bishop.base.Move;
import bishop.base.MoveStack;
import bishop.base.Position;
import bishop.base.PseudoLegalMoveGenerator;


/**
 * MateFinder uses a simple implementation of alpha-beta algorithm to find a forced mate in
 * a series of moves check - reaction to check - check ...
 *   
 * @author Ing. Petr Ležák
 */
public class MateFinder {

	private static final double HISTORY_COEFF = 1000;

	private class NodeRecord {
		private final int depth;
		private final NodeRecord nextRecord;
		private int nonLosingMoveCount;   // Numbers of moves that does not lead to mate
		private int losingMoveEvaluation;   // Evaluation of losing moves
		private final Move killerMove = new Move();
		private final Move move = new Move();
		private final Move nonLosingMove = new Move();   // Single moves that does not lead to mate

		public NodeRecord(final int depth, final NodeRecord nextRecord) {
			this.depth = depth;
			this.nextRecord = nextRecord;
		}

		public int findMate(final int horizon, final int extension, final int alpha, final int beta) {
			nonLosingMoveCount = 0;
			losingMoveEvaluation = Evaluation.MIN;

			if (position.isKingNotOnTurnAttacked())
				return Evaluation.MAX;

			if (horizon < 0)
				return (isMate()) ? -Evaluation.getMateEvaluation(depth + depthAdvance) : Evaluation.DRAW;

			final boolean isAttacker = position.getOnTurn() == attackerColor;
			boolean mustBeCheck = false;

			if (!isAttacker) {
				if (!position.isCheck())
					return Evaluation.DRAW;
				else
					mustBeCheck = true;
			}

			boolean existLegalMove = false;
			int updatedAlpha = alpha;
			int effectiveAlpha = alpha;
			int evaluation = Evaluation.MIN;

			final boolean singularExtensionPossible = extension + 2 <= maxExtension;

			if (killerMove.uncompressMove(killerMoves[depth + depthAdvance], position)) {
				final int subEvaluation = evaluateMove(horizon, extension, effectiveAlpha, beta, killerMove);

				existLegalMove = (subEvaluation > Evaluation.MIN);
				updatedAlpha = Math.max(updatedAlpha, subEvaluation);
				evaluation = Math.max(evaluation, subEvaluation);

				effectiveAlpha = calculateEffectiveAlpha(alpha, updatedAlpha, isAttacker, singularExtensionPossible);

				if (effectiveAlpha > beta)
					return subEvaluation;
			}

			final int moveStackBegin = moveStackTop;
			moveGenerator.setGenerateOnlyChecks(isAttacker);
			moveGenerator.setReduceMovesInCheck(!isAttacker);
			moveGenerator.generateMoves();

			int moveStackEnd = moveStackTop;

			moveStack.sortMoves(moveStackBegin, moveStackEnd);

			while (moveStackEnd > moveStackBegin) {
				moveStack.getMove(moveStackEnd - 1, move);

				if (!move.equals(killerMove)) {
					moveStackTop = moveStackEnd;
					final int subEvaluation = evaluateMove(horizon, extension, effectiveAlpha, beta, move);

					evaluation = Math.max(evaluation, subEvaluation);

					existLegalMove |= (subEvaluation > Evaluation.MIN);
					updatedAlpha = Math.max(updatedAlpha, subEvaluation);

					effectiveAlpha = calculateEffectiveAlpha(alpha, updatedAlpha, isAttacker, singularExtensionPossible);

					if (effectiveAlpha > beta) {
						killerMoves[depth + depthAdvance] = move.getCompressedMove();
						moveStackTop = moveStackBegin;
						historyTable.addCutoff(position.getOnTurn(), move, horizon);

						return evaluation;
					}
				}

				moveStackEnd--;
			}

			moveStackTop = moveStackBegin;

			if (existLegalMove) {
				// Singular extension
				if (!isAttacker && nonLosingMoveCount == 1 && singularExtensionPossible)
					evaluation = evaluateMove(horizon + 2, extension + 2, losingMoveEvaluation, beta, nonLosingMove);

				return evaluation;
			}
			else {
				if (mustBeCheck || position.isCheck())
					return -Evaluation.getMateEvaluation(depth + depthAdvance);
				else
					return Evaluation.DRAW;
			}
		}

		private int calculateEffectiveAlpha (final int originalAlpha, final int updatedAlpha, final boolean isAttacker, final boolean singularExtensionPossible) {
			if (isAttacker || nonLosingMoveCount > 1 || (depth > 0 && !singularExtensionPossible))
				return updatedAlpha;
			else
				return Math.max(losingMoveEvaluation, originalAlpha);
		}

		private int evaluateMove(final int horizon, final int extension, int alpha, final int beta, final Move move) {
			position.makeMove(move);

			final int subHorizon = SerialSearchEngine.matePrunning(depth, horizon - 1, alpha, beta);
			final int subEvaluation = -nextRecord.findMate(subHorizon, extension, -beta, -alpha);
			position.undoMove(move);

			if (subEvaluation > -Evaluation.MATE_MIN) {
				nonLosingMoveCount++;
				nonLosingMove.assign(move);
			}
			else
				losingMoveEvaluation = Math.max(losingMoveEvaluation, subEvaluation);

			return subEvaluation;
		}
	}
	
	private int maxDepth;
	private int maxExtension;
	private MoveStack moveStack;
	private int moveStackTop;
	private int[] killerMoves;   // Killer moves for given depth
	private final HistoryTable historyTable = new HistoryTable();
	private int depthAdvance;
	private int attackerColor;   // Color of the side that gives mate
	private NodeRecord[] nodeRecords;

	private final PseudoLegalMoveGenerator moveGenerator;
	private final MateChecker mateChecker = new MateChecker();
	
	private Position position;
	
	
	public MateFinder() {
		this.moveGenerator = new PseudoLegalMoveGenerator();
		this.moveGenerator.setWalker(this::processMove);
	}

	private boolean processMove(final Move move) {
		final int history = (int) (HISTORY_COEFF * historyTable.getEvaluation(position.getOnTurn(), move));
		moveStack.setRecord(moveStackTop, move, history);
		moveStackTop++;

		return true;
	}
	
	public void setPosition (final Position position) {
		this.position = position;
		this.moveGenerator.setPosition(position);
	}
	
	public boolean isMate() {
		return mateChecker.isMate(position);
	}
	
	public int findWin(final int depthInMoves) {
		final int horizon = 2 * depthInMoves - 2;
		
		if (horizon > maxDepth)
			throw new RuntimeException("Too deep");
		
		moveStackTop = 0;
		attackerColor = position.getOnTurn();
		
		return nodeRecords[0].findMate(horizon, 0, Evaluation.MATE_MIN, Evaluation.MAX);
	}
	
	/**
	 * Finds lose. Must be called on position with check.
	 * @param depthInMoves
	 * @return
	 */
	public int findLose(final int depthInMoves) {
		final int horizon = 2 * depthInMoves - 1;
		
		if (horizon > maxDepth)
			throw new RuntimeException("Too deep");
		
		moveStackTop = 0;
		attackerColor = Color.getOppositeColor(position.getOnTurn());
		
		return nodeRecords[0].findMate(horizon, 0, Evaluation.MIN, -Evaluation.MATE_MIN);
	}

	public void setMaxDepth (final int maxDepthInMoves, final int maxDepthAdvance, final int maxExtensionInMoves) {
		this.maxDepth = 2 * maxDepthInMoves - 1;
		this.maxExtension = 2 * maxExtensionInMoves;
		
		final int maxExtendedDepth = maxDepth + maxExtension + 1;
		moveStack = new MoveStack((maxExtendedDepth + 1) * PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION);
		
		this.killerMoves = new int[maxExtendedDepth + maxDepthAdvance + 1];
		this.nodeRecords = new NodeRecord[maxExtendedDepth + 1];

		NodeRecord nextRecord = null;
		
		for (int i = maxExtendedDepth; i >= 0; i--) {
			this.nodeRecords[i] = new NodeRecord(i, nextRecord);
			nextRecord = nodeRecords[i];
		}
	}
	
	public void setDepthAdvance (final int depthAdvance) {
		this.depthAdvance = depthAdvance;
	}
	
	public void clearKillerMoves() {
		Arrays.fill(killerMoves, Move.NONE_COMPRESSED_MOVE);
	}
	
	public int getNonLosingMoveCount() {
		return nodeRecords[0].nonLosingMoveCount;
	}
	
	public void getNonLosingMove(final Move move) {
		move.assign(nodeRecords[0].nonLosingMove);
	}
	
	public int getLosingMovesEvaluation() {
		return nodeRecords[0].losingMoveEvaluation;
	}
	
}
