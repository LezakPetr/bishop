package bishop.tables;

import bishop.base.File;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Rank;
import bishop.base.Square;

public class MatedKingEvaluationTable {
	
	private static final double MATED_KING_SIDE_DISTANCE_EVALUATION = 0.012;
	private static final double MATED_KING_CORNER_DISTANCE_EVALUATION = 0.002;

	private static final int[] table = createTable();
	
	
	public static int getItem (final int square) {
		return table[square];
	}

	private static int[] createTable() {
		final int[] table = new int[Square.LAST];
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final int file = Square.getFile(square);
			final int rank = Square.getRank(square);
			
			final int fileDistance = Math.min(file - File.FA, File.FH - file);
			final int rankDistance = Math.min(rank - Rank.R1, Rank.R8 - rank);
			
			final int sideDistance = Math.min(fileDistance, rankDistance);
			final int cornerDistance = Math.max(fileDistance, rankDistance);
			
			final double totalEvaluation = -PieceTypeEvaluations.PAWN_EVALUATION * (MATED_KING_SIDE_DISTANCE_EVALUATION * sideDistance + MATED_KING_CORNER_DISTANCE_EVALUATION * cornerDistance);
			table[square] = (int) Math.round(totalEvaluation);
		}
		
		return table;
	}

}
