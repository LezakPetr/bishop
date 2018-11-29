package bishop.engine;

import java.util.Arrays;

import bishop.base.Color;
import bishop.base.IMoveWalker;
import bishop.base.MateChecker;
import bishop.base.Move;
import bishop.base.MoveList;
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
	
	private int maxDepth;
	private int maxExtension;
	private MoveStack moveStack;
	private int moveStackTop;
	private int[] killerMoves;   // Killer moves for given depth
	private final HistoryTable historyTable = new HistoryTable();
	private int[] nonLosingMoveCounts;   // Numbers of moves that does not lead to mate for given depth
	private int[] losingMovesEvaluations;   // Evaluation of losing moves for given depth
	private final MoveList nonLosingMoves;   // Single moves that does not lead to mate for given depth
	private int depthAdvance;
	private int attackerColor;   // Color of the side that gives mate

	// Pre-created objects to prevent allocation
	private Move[] precreatedKillerMoves;
	private Move[] precreatedMoves;
	
	private final IMoveWalker walker = new IMoveWalker() {
		public boolean processMove(final Move move) {
			moveStack.setRecord(moveStackTop, move, historyTable.getEvaluation(position.getOnTurn(), move));
			moveStackTop++;

			return true;
		}
	};
	
	private final PseudoLegalMoveGenerator moveGenerator;
	private final MateChecker mateChecker = new MateChecker();
	
	private Position position;
	
	
	public MateFinder() {
		this.moveGenerator = new PseudoLegalMoveGenerator();
		this.moveGenerator.setWalker(walker);
		this.nonLosingMoves = new MoveList();
	}
	
	public void setPosition (final Position position) {
		this.position = position;
		this.moveGenerator.setPosition(position);
	}
	
	public boolean isMate() {
		return mateChecker.isMate(position);
	}
	
	private int calculateEffectiveAlpha (final int depth, final int originalAlpha, final int updatedAlpha, final boolean isAttacker, final boolean singularExtensionPossible) {
		if (isAttacker || nonLosingMoveCounts[depth] > 1 || (depth > 0 && !singularExtensionPossible))
			return updatedAlpha;
		else
			return Math.max(losingMovesEvaluations[depth], originalAlpha);
	}
	
	private int findMate(final int depth, final int horizon, final int extension, final int alpha, final int beta) {
		nonLosingMoveCounts[depth] = 0;
		losingMovesEvaluations[depth] = Evaluation.MIN;

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
		
		final Move killerMove = precreatedKillerMoves[depth];
		boolean existLegalMove = false;
		int updatedAlpha = alpha;
		int effectiveAlpha = alpha;
		int evaluation = Evaluation.MIN;
		
		final boolean singularExtensionPossible = extension + 2 <= maxExtension;
		
		if (killerMove.uncompressMove(killerMoves[depth + depthAdvance], position)) {
			final int subEvaluation = evaluateMove(depth, horizon, extension, effectiveAlpha, beta, killerMove);
			
			existLegalMove |= (subEvaluation > Evaluation.MIN);
			updatedAlpha = Math.max(updatedAlpha, subEvaluation);
			evaluation = Math.max(evaluation, subEvaluation);
			
			effectiveAlpha = calculateEffectiveAlpha(depth, alpha, updatedAlpha, isAttacker, singularExtensionPossible);
			
			if (effectiveAlpha > beta)
				return subEvaluation;
		}
		
		final int moveStackBegin = moveStackTop;
		moveGenerator.setGenerateOnlyChecks(isAttacker);
		moveGenerator.setReduceMovesInCheck(!isAttacker);
		moveGenerator.generateMoves();

		int moveStackEnd = moveStackTop;

		moveStack.sortMoves (moveStackBegin, moveStackEnd);
		
		final Move move = precreatedMoves[depth];
		
		while (moveStackEnd > moveStackBegin) {
			moveStack.getMove(moveStackEnd - 1, move);
			
			if (!move.equals(killerMove)) {
				moveStackTop = moveStackEnd;
				final int subEvaluation = evaluateMove(depth, horizon, extension, effectiveAlpha, beta, move);
				
				evaluation = Math.max(evaluation, subEvaluation);
				
				existLegalMove |= (subEvaluation > Evaluation.MIN);
				updatedAlpha = Math.max(updatedAlpha, subEvaluation);
			
				effectiveAlpha = calculateEffectiveAlpha(depth, alpha, updatedAlpha, isAttacker, singularExtensionPossible);
				
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
			if (!isAttacker && nonLosingMoveCounts[depth] == 1 && singularExtensionPossible)
				evaluation = evaluateMove(depth, horizon + 2, extension + 2, losingMovesEvaluations[depth], beta, nonLosingMoves.get(depth));
			
			return evaluation;
		}
		else {
			if (mustBeCheck || position.isCheck())
				return -Evaluation.getMateEvaluation(depth + depthAdvance);
			else
				return Evaluation.DRAW;
		}
	}

	public int evaluateMove(final int depth, final int horizon, final int extension, int alpha, final int beta, final Move move) {
		position.makeMove(move);
		
		final int subHorizon = SerialSearchEngine.matePrunning(depth, horizon - 1, alpha, beta, 1);
		final int subEvaluation = -findMate(depth + 1, subHorizon, extension, -beta, -alpha);
		position.undoMove(move);
		
		if (subEvaluation > -Evaluation.MATE_MIN) {
			nonLosingMoveCounts[depth]++;
			nonLosingMoves.set(depth, move);
		}
		else
			losingMovesEvaluations[depth] = Math.max(losingMovesEvaluations[depth], subEvaluation);
		
		return subEvaluation;
	}
	
	public int findWin(final int depthInMoves) {
		final int horizon = 2 * depthInMoves - 2;
		
		if (horizon > maxDepth)
			throw new RuntimeException("Too deep");
		
		moveStackTop = 0;
		attackerColor = position.getOnTurn();
		
		return findMate(0, horizon, 0, Evaluation.MATE_MIN, Evaluation.MAX);
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
		
		return findMate(0, horizon, 0, Evaluation.MIN, -Evaluation.MATE_MIN);
	}

	public void setMaxDepth (final int maxDepthInMoves, final int maxDepthAdvance, final int maxExtensionInMoves) {
		this.maxDepth = 2 * maxDepthInMoves - 1;
		this.maxExtension = 2 * maxExtensionInMoves;
		
		final int maxExtendedDepth = maxDepth + maxExtension;
		moveStack = new MoveStack((maxExtendedDepth + 1) * PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION);
		
		this.killerMoves = new int[maxExtendedDepth + maxDepthAdvance + 1];
		this.nonLosingMoveCounts = new int[maxExtendedDepth + 2];
		this.losingMovesEvaluations = new int[maxExtendedDepth + 2];
		this.precreatedKillerMoves = new Move[maxExtendedDepth + 1];
		this.precreatedMoves = new Move[maxExtendedDepth + 1];
		
		this.nonLosingMoves.reserve(maxExtendedDepth + 1);
		
		for (int i = 0; i <= maxExtendedDepth; i++) {
			this.nonLosingMoves.add(new Move());
			this.precreatedKillerMoves[i] = new Move();
			this.precreatedMoves[i] = new Move();
		}
	}
	
	public void setDepthAdvance (final int depthAdvance) {
		this.depthAdvance = depthAdvance;
	}
	
	public void clearKillerMoves() {
		Arrays.fill(killerMoves, Move.NONE_COMPRESSED_MOVE);
	}
	
	public int getNonLosingMoveCount() {
		return nonLosingMoveCounts[0];
	}
	
	public void getNonLosingMove(final Move move) {
		nonLosingMoves.assignToMove(0, move);
	}
	
	public int getLosingMovesEvaluation() {
		return losingMovesEvaluations[0];
	}
	
}
