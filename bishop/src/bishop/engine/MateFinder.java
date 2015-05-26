package bishop.engine;

import java.util.Arrays;

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
	
	private final IMoveWalker walker = new IMoveWalker() {
		public boolean processMove(final Move move) {
			moveStack.setRecord(moveStackTop, move, 0);
			moveStackTop++;

			return true;
		}
	};

	
	private final PseudoLegalMoveGenerator moveGenerator;
	private final LegalMoveFinder legalMoveFinder = new LegalMoveFinder();
	
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
			return Evaluation.DRAW;
		
		final Move killerMove = new Move();
		boolean existLegalMove = false;
		int updatedAlpha = alpha;
		int evaluation = Evaluation.MIN;
		
		if (killerMove.uncompressMove(killerMoves[depth], position)) {
			position.makeMove(killerMove);
			final int subEvaluation = -findMate(depth + 1, horizon - 1, -beta, -updatedAlpha);
			position.undoMove(killerMove);
			
			if (subEvaluation > beta)
				return subEvaluation;
			
			existLegalMove |= (subEvaluation > Evaluation.MIN);
			updatedAlpha = Math.max(updatedAlpha, subEvaluation);
			evaluation = Math.max(evaluation, subEvaluation);
		}
		
		final int moveStackBegin = moveStackTop;
		moveGenerator.generateMoves();
		final int moveStackEnd = moveStackTop;
		
		final Move move = new Move();
		
		for (int i = moveStackBegin; i < moveStackEnd; i++) {
			moveStack.getMove(i, move);
			
			if (!move.equals(killerMove)) {
				position.makeMove(move);
				final int subEvaluation = -findMate(depth + 1, horizon - 1, -beta, -updatedAlpha);
				position.undoMove(move);
				
				evaluation = Math.max(evaluation, subEvaluation);
				
				if (subEvaluation > beta) {
					killerMoves[depth] = move.getCompressedMove();
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
				return -Evaluation.getMateEvaluation(depth);
			else
				return Evaluation.DRAW;
		}
	}
	
	public boolean isWin(final int depthInMoves) {
		final int horizon = 2 * depthInMoves - 1;
		
		if (horizon > maxDepth)
			throw new RuntimeException("Too deep");
		
		moveStackTop = 0;
		Arrays.fill(killerMoves, Move.NONE_COMPRESSED_MOVE);
		
		final int evaluation = findMate(0, horizon, Evaluation.MATE_MIN, Evaluation.MATE_MIN);
		
		return evaluation >= Evaluation.MATE_MIN;
	}
	
	public void setMaxDepth (final int maxDepthInMoves) {
		this.maxDepth = 2 * maxDepthInMoves - 1;
		moveStack = new MoveStack((maxDepth + 1) * PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION);
		
		this.killerMoves = new int[maxDepth + 1];
	}
	
}
