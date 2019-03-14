package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public final class BishopColorPositionEvaluator {
		
	private final GameStageCoeffs coeffs;
	private final IPositionEvaluation evaluation;
	
	
	public BishopColorPositionEvaluator(final GameStageCoeffs coeffs, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.coeffs = coeffs;
		this.evaluation = evaluationFactory.get();		
	}

	public IPositionEvaluation evaluatePosition(final Position position) {
		evaluation.clear();
		
		for (int pieceColor = Color.FIRST; pieceColor < Color.LAST; pieceColor++) {
			evaluatePositionForColor(position, pieceColor);
		}
		
		return evaluation;
	}
	
	private void evaluatePositionForColor(final Position position, final int pieceColor) {
		final long bishopMask = position.getPiecesMask(pieceColor, PieceType.BISHOP);
		
		if (bishopMask != 0) {
			final long pawnMask = position.getPiecesMask(pieceColor, PieceType.PAWN);
			
			for (int squareColor = Color.FIRST; squareColor < Color.LAST; squareColor++) {
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
