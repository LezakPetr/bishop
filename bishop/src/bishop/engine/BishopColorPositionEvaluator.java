package bishop.engine;

import java.io.PrintWriter;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Position;
import bishop.base.SquareColor;

public final class BishopColorPositionEvaluator {
	
	private static final double PAWN_ON_SAME_COLOR_BONUS = -0.05;
	
	private final int[] pawnOnSameColorBonus;
	private int evaluation;
	
	
	public BishopColorPositionEvaluator() {
		pawnOnSameColorBonus = new int[SquareColor.LAST];
		
		for (int color = SquareColor.FIRST; color < SquareColor.LAST; color++) {
			final int pawnEvaluation = PieceTypeEvaluations.getPieceEvaluation(color, PieceType.PAWN);
			
			pawnOnSameColorBonus[color] = (int) Math.round (pawnEvaluation * PAWN_ON_SAME_COLOR_BONUS);
		}
	}

	public int evaluatePosition(final Position position) {
		evaluation = 0;
		
		for (int pieceColor = Color.FIRST; pieceColor < Color.LAST; pieceColor++) {
			final long bishopMask = position.getPiecesMask(pieceColor, PieceType.BISHOP);
			
			for (int squareColor = SquareColor.FIRST; squareColor < SquareColor.LAST; squareColor++) {
				final long squareMask = BoardConstants.getSquareColorMask(squareColor);
				
				if ((bishopMask & squareMask) != 0) {
					final long pawnMask = position.getPiecesMask(pieceColor, PieceType.PAWN);
					final int pawnCount = BitBoard.getSquareCount(pawnMask & squareMask);
					
					evaluation += pawnCount * pawnOnSameColorBonus[pieceColor];
				}
			}
		}
		
		return evaluation;
	}

	public void writeLog(final PrintWriter writer) {
		writer.println ("Bishop color evaluation: " + Evaluation.toString(evaluation));
	}

}
