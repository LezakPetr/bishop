package bishop.tables;

import bishop.base.BoardConstants;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Square;

public class MatingKingEvaluationTable {
	
	private static final int[] MATING_KING_OPTIMAL_SQUARES = {
		//     a          b          c          d          e          f          g          h
		Square.B2, Square.B3, Square.C3, Square.D3, Square.E3, Square.F3, Square.G3, Square.G2,   // 1
		Square.C2, Square.C3, Square.C4, Square.D4, Square.E4, Square.F4, Square.F3, Square.F2,   // 2
		Square.C3, Square.D3, Square.D4, Square.D5, Square.E5, Square.E4, Square.E3, Square.F3,   // 3
		Square.C4, Square.D4, Square.E4, Square.E5, Square.D5, Square.D4, Square.E4, Square.F4,   // 4
		Square.C5, Square.D5, Square.E5, Square.E4, Square.D4, Square.D5, Square.E5, Square.F5,   // 5
		Square.C6, Square.D6, Square.D5, Square.D4, Square.E4, Square.E5, Square.E6, Square.F6,   // 6
		Square.C7, Square.C6, Square.C5, Square.D5, Square.E5, Square.F5, Square.F6, Square.F7,   // 7
		Square.B7, Square.B6, Square.C6, Square.D6, Square.E6, Square.F6, Square.G6, Square.G7    // 8
	};
	
	private static final double MATING_KING_DISTANCE_EVALUATION = 0.002;
	
	private static final int MATING_KING_SQUARE_SHIFT = 0;
	private static final int MATED_KING_SQUARE_SHIFT = MATING_KING_SQUARE_SHIFT + Square.BIT_COUNT;
	private static final int BIT_COUNT = MATED_KING_SQUARE_SHIFT + Square.BIT_COUNT;
	

	private static int getItemIndex (final int matedKingSquare, final int matingKingSquare) {
		return matingKingSquare + (matedKingSquare << MATED_KING_SQUARE_SHIFT);
	}
	
	public static int getItem (final int matedKingSquare, final int matingKingSquare) {
		return table[(matingKingSquare) + (matedKingSquare << 6)];
	}
	
	private static final int[] table = createTable();
		
	private static int[] createTable() {
		final int size = 1 << BIT_COUNT;
		final int[] table = new int[size];
		
		for (int matedKingSquare = Square.FIRST; matedKingSquare < Square.LAST; matedKingSquare++) {
			for (int matingKingSquare = Square.FIRST; matingKingSquare < Square.LAST; matingKingSquare++) {
				final int optimalSquare = MATING_KING_OPTIMAL_SQUARES[matedKingSquare];
				final int distance = BoardConstants.getKingSquareDistance (matingKingSquare, optimalSquare);
	
				final double currentEvaluation = -distance * MATING_KING_DISTANCE_EVALUATION * PieceTypeEvaluations.PAWN_EVALUATION;;
				final int roundedEvaluation = (int) Math.round(currentEvaluation);
				
				table[getItemIndex (matedKingSquare, matingKingSquare)] = roundedEvaluation;
			}
		}
	
		return table;
	}

}
