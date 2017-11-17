package bishop.engine;

import java.util.Arrays;

import bishop.base.Color;
import bishop.base.IMoveWalker;
import bishop.base.LegalMoveFinder;
import bishop.base.Move;
import bishop.base.MoveStack;
import bishop.base.Position;
import bishop.base.PseudoLegalMoveGenerator;

public class MateFinder {
	
	private int maxDepth;
	private MoveStack moveStack;
	private int moveStackTop;
	private int[] killerMoves;
	private int depthAdvance;
	private int attackerColor;
	
	private final IMoveWalker walker = new IMoveWalker() {
		public boolean processMove(final Move move) {
			moveStack.setRecord(moveStackTop, move, 0);
			moveStackTop++;

			return true;
		}
	};
	
	private final PseudoLegalMoveGenerator moveGenerator;
	private final LegalMoveFinder legalMoveFinder = new LegalMoveFinder(true);
	
	private Position position;
	
	
	public MateFinder() {
		this.moveGenerator = new PseudoLegalMoveGenerator();
		this.moveGenerator.setWalker(walker);
	}
	
	public void setPosition (final Position position) {
		this.position = position;
		this.moveGenerator.setPosition(position);
	}
	
	public boolean isMate() {
		return position.isCheck() && !legalMoveFinder.existsLegalMove(position);
	}
	
	private int findMate(final int depth, final int horizon, final int alpha, final int beta) {
		if (position.isKingNotOnTurnAttacked())
			return Evaluation.MAX;
		
		if (horizon < 0)
			return (isMate()) ? -Evaluation.getMateEvaluation(depth + depthAdvance) : Evaluation.DRAW;
		
		final boolean isCheck = position.isCheck();
		final boolean isAttacker = position.getOnTurn() == attackerColor;
		
		if (!isCheck && !isAttacker)
			return Evaluation.DRAW;
		
		final Move killerMove = new Move();
		boolean existLegalMove = false;
		int updatedAlpha = alpha;
		int evaluation = Evaluation.MIN;
		
		if (killerMove.uncompressMove(killerMoves[depth + depthAdvance], position)) {
			final int subEvaluation = evaluateMove(depth, horizon, updatedAlpha, beta, killerMove);
			
			if (subEvaluation > beta)
				return subEvaluation;
			
			existLegalMove |= (subEvaluation > Evaluation.MIN);
			updatedAlpha = Math.max(updatedAlpha, subEvaluation);
			evaluation = Math.max(evaluation, subEvaluation);
		}
		
		final int moveStackBegin = moveStackTop;
		moveGenerator.setGenerateOnlyChecks(isAttacker);
		moveGenerator.setReduceMovesInCheck(!isAttacker);
		moveGenerator.generateMoves();
		final int moveStackEnd = moveStackTop;
		
		final Move move = new Move();
		
		for (int i = moveStackBegin; i < moveStackEnd; i++) {
			moveStack.getMove(i, move);
			
			if (!move.equals(killerMove)) {
				final int subEvaluation = evaluateMove(depth, horizon, updatedAlpha, beta, move);
				
				evaluation = Math.max(evaluation, subEvaluation);
				
				if (subEvaluation > beta) {
					killerMoves[depth + depthAdvance] = move.getCompressedMove();
					moveStackTop = moveStackBegin;
					
					return evaluation;
				}
				
				existLegalMove |= (subEvaluation > Evaluation.MIN);
				updatedAlpha = Math.max(updatedAlpha, subEvaluation);
			}
		}
		
		moveStackTop = moveStackBegin;
		
		if (existLegalMove)
			return evaluation;
		else {
			if (position.isCheck())
				return -Evaluation.getMateEvaluation(depth + depthAdvance);
			else
				return Evaluation.DRAW;
		}
	}

	public int evaluateMove(final int depth, final int horizon, int alpha, final int beta, final Move move) {
		position.makeMove(move);
		
		final int subHorizon = SerialSearchEngine.matePrunning(depth, horizon - 1, alpha, beta, 1);
		final int subEvaluation = -findMate(depth + 1, subHorizon, -beta, -alpha);
		position.undoMove(move);
		
		return subEvaluation;
	}
	
	public int findWin(final int depthInMoves) {
		final int horizon = 2 * depthInMoves - 2;
		
		if (horizon > maxDepth)
			throw new RuntimeException("Too deep");
		
		moveStackTop = 0;
		attackerColor = position.getOnTurn();
		
		return findMate(0, horizon, Evaluation.MATE_MIN, Evaluation.MAX);
	}
	
	/**
	 * Finds lose. Can be called on position with check.
	 * @param depthInMoves
	 * @return
	 */
	public int findLose(final int depthInMoves) {
		final int horizon = 2 * depthInMoves - 1;
		
		if (horizon > maxDepth)
			throw new RuntimeException("Too deep");
		
		moveStackTop = 0;
		attackerColor = Color.getOppositeColor(position.getOnTurn());
		
		return findMate(0, horizon, Evaluation.MIN, -Evaluation.MATE_MIN);
	}

	public void setMaxDepth (final int maxDepthInMoves, final int maxDepthAdvance) {
		this.maxDepth = 2 * maxDepthInMoves - 1;
		moveStack = new MoveStack((maxDepth + 1) * PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION);
		
		this.killerMoves = new int[maxDepth + maxDepthAdvance + 1];
	}
	
	public void setDepthAdvance (final int depthAdvance) {
		this.depthAdvance = depthAdvance;
	}
	
	public void clearKillerMoves() {
		Arrays.fill(killerMoves, Move.NONE_COMPRESSED_MOVE);
	}
	
}
