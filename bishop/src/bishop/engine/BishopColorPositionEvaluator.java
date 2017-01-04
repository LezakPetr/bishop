package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

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
	private final IPositionEvaluation evaluation;
	
	
	public BishopColorPositionEvaluator(final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluation = evaluationFactory.get();
		
		pawnOnSameColorBonus = new int[SquareColor.LAST];
		
		for (int color = SquareColor.FIRST; color < SquareColor.LAST; color++) {
			final int pawnEvaluation = PieceTypeEvaluations.getPieceEvaluation(color, PieceType.PAWN);
			
			pawnOnSameColorBonus[color] = (int) Math.round (pawnEvaluation * PAWN_ON_SAME_COLOR_BONUS);
		}
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
		
		for (int squareColor = SquareColor.FIRST; squareColor < SquareColor.LAST; squareColor++) {
			final long squareMask = BoardConstants.getSquareColorMask(squareColor);
			
			if ((bishopMask & squareMask) != 0) {
				final long pawnMask = position.getPiecesMask(pieceColor, PieceType.PAWN);
				final int pawnCount = BitBoard.getSquareCount(pawnMask & squareMask);
			
				evaluation.addCoeff(PositionEvaluationCoeffs.PAWN_ON_SAME_COLOR_BONUS, pieceColor, pawnCount);
			}
		}
	}

	public void writeLog(final PrintWriter writer) {
		writer.println ("Bishop color evaluation: " + evaluation.toString());
	}

}
