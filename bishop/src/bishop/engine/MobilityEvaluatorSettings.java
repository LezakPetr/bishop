package bishop.engine;

import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;

public class MobilityEvaluatorSettings {

	private final int[][] mobilityTable;
	
	public MobilityEvaluatorSettings() {
		mobilityTable = new int[Color.LAST][PieceType.LAST];
		setMobilityTable();
	}

	private void setMobilityTable() {
		final double[] mobilityBonus = new double[PieceType.LAST];
		mobilityBonus[PieceType.BISHOP] = 0.02;
		mobilityBonus[PieceType.KNIGHT] = 0.04;
		mobilityBonus[PieceType.ROOK] = 0.015;
		mobilityBonus[PieceType.QUEEN] = 0.01;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int pawnEvaluation = PieceTypeEvaluations.getPieceEvaluation(color, PieceType.PAWN);
			
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++)
				mobilityTable[color][pieceType] = (int) Math.round (pawnEvaluation * mobilityBonus[pieceType]);
		}
	}
	
	public int getMobilityEvaluation(final int color, final int pieceType) {
		return mobilityTable[color][pieceType];
	}
	
}
