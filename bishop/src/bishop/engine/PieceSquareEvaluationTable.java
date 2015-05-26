package bishop.engine;

import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Square;

public class PieceSquareEvaluationTable {

	private final int[] table;
	
	
	private static final int PIECE_TYPE_SHIFT = 6;
	private static final int COLOR_SHIFT = 9;
	private static final int TOTAL_BITS = 1 + 3 + 6;
	
	private int getIndex (final int color, final int pieceType, final int square) {
		return (color << COLOR_SHIFT) + (pieceType << PIECE_TYPE_SHIFT) + square;
	}
	
	public PieceSquareEvaluationTable() {
		table = new int[1 << TOTAL_BITS];
	}
	
	public int getEvaluation (final int color, final int pieceType, final int square) {
		final int index = getIndex(color, pieceType, square);
		
		return table[index];
	}
	
	public void setEvaluation (final double[] constantEvaluation, final double[][] squareEvaluation, final double[] pieceTypeCoeffs) {
		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				double totalEvaluation = 0.0;
				
				if (constantEvaluation != null)
					totalEvaluation += constantEvaluation[pieceType];
				
				if (squareEvaluation[pieceType] != null)
					totalEvaluation += squareEvaluation[pieceType][square] * pieceTypeCoeffs[pieceType];
				
				final int evaluation = (int) Math.round(PieceTypeEvaluations.PAWN_EVALUATION * totalEvaluation);
				final int oppositeSquare = Square.getOppositeSquare(square);
				
				final int indexWhite = getIndex(Color.WHITE, pieceType, square);
				table[indexWhite] = evaluation;
				
				final int indexBlack = getIndex(Color.BLACK, pieceType, oppositeSquare);
				table[indexBlack] = -evaluation;
			}
		}

	}
}
