package bishop.engine;

import java.io.PrintWriter;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.SquareColor;

public final class BishopColorPositionEvaluator {
		
	private final GameStageFeatures coeffs;
	private final IPositionEvaluation evaluation;
	
	
	public BishopColorPositionEvaluator(final GameStageFeatures coeffs, final IPositionEvaluation evaluation) {
		this.coeffs = coeffs;
		this.evaluation = evaluation;		
	}

	public void evaluatePosition(final Position position) {
		for (int pieceColor = Color.FIRST; pieceColor < Color.LAST; pieceColor++) {
			evaluatePositionForColor(position, pieceColor);
		}
	}
	
	private void evaluatePositionForColor(final Position position, final int pieceColor) {
		final long bishopMask = position.getPiecesMask(pieceColor, PieceType.BISHOP);
		
		if (bishopMask != 0) {
			final long pawnMask = position.getPiecesMask(pieceColor, PieceType.PAWN);
			
			for (int squareColor = SquareColor.FIRST; squareColor < SquareColor.LAST; squareColor++) {
				final long squareMask = BoardConstants.getSquareColorMask(squareColor);
				
				if ((bishopMask & squareMask) != 0) {
					final int pawnCount = BitBoard.getSquareCount(pawnMask & squareMask);
				
					evaluation.addCoeff(coeffs.pawnOnSameColorBonus, pieceColor, pawnCount);
				}
			}
		}
	}

	public void writeLog(final PrintWriter writer) {
		writer.println ("Bishop color evaluation: " + evaluation.toString());
	}

}
