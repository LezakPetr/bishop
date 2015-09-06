package bishop.engine;

import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Rank;
import bishop.base.Square;

public final class EndingPositionEvaluatorSettings {

	private final TablePositionEvaluatorSettings tableSettings;
	private final PawnStructureEvaluatorSettings pawnStructureEvaluatorSettings;
	
	private final int[] ruleOfSquareBonus;
	
	private final static double RULE_OF_SQUARE_BONUS = 5.0;
	private final static double DOUBLE_PAWN_BONUS = -0.4;
	
	
	public EndingPositionEvaluatorSettings() {
		tableSettings = new TablePositionEvaluatorSettings();
		pawnStructureEvaluatorSettings = new PawnStructureEvaluatorSettings();

		ruleOfSquareBonus = new int[Color.LAST];

		setDefaultTables();
		setDefaultPawnTables();
	}

	public TablePositionEvaluatorSettings getTableSettings() {
		return tableSettings;
	}
	
	private void setDefaultTables() {
		double[][] squareEvaluation = new double[PieceType.LAST][];
		
		squareEvaluation[PieceType.PAWN] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 1
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 2
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 3
			0.050, 0.100, 0.100, 0.100, 0.100, 0.100, 0.100, 0.050,   // 4
			0.100, 0.200, 0.200, 0.200, 0.200, 0.200, 0.200, 0.100,   // 5
			0.200, 0.400, 0.400, 0.400, 0.400, 0.400, 0.400, 0.200,   // 6
			0.400, 0.800, 0.800, 0.800, 0.800, 0.800, 0.800, 0.400,   // 7
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000    // 8
		};
		
		squareEvaluation[PieceType.KNIGHT] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 1
			0.000, 0.300, 0.300, 0.300, 0.300, 0.300, 0.300, 0.000,   // 2
			0.000, 0.300, 0.700, 0.700, 0.700, 0.700, 0.300, 0.000,   // 3
			0.000, 0.300, 0.700, 1.000, 1.000, 0.700, 0.300, 0.000,   // 4
			0.000, 0.300, 0.700, 1.000, 1.000, 0.700, 0.300, 0.000,   // 5
			0.000, 0.300, 0.700, 0.700, 0.700, 0.700, 0.300, 0.000,   // 6
			0.000, 0.300, 0.300, 0.300, 0.300, 0.300, 0.300, 0.000,   // 7
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000    // 8
		};
		
		squareEvaluation[PieceType.BISHOP] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 1
			0.000, 0.300, 0.300, 0.300, 0.300, 0.300, 0.300, 0.000,   // 2
			0.000, 0.300, 0.700, 0.700, 0.700, 0.700, 0.300, 0.000,   // 3
			0.000, 0.300, 0.700, 1.000, 1.000, 0.700, 0.300, 0.000,   // 4
			0.000, 0.300, 0.700, 1.000, 1.000, 0.700, 0.300, 0.000,   // 5
			0.000, 0.300, 0.700, 0.700, 0.700, 0.700, 0.300, 0.000,   // 6
			0.000, 0.300, 0.300, 0.300, 0.300, 0.300, 0.300, 0.000,   // 7
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000    // 8
		};
		
		squareEvaluation[PieceType.ROOK] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 1
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 2
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 3
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 4
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 5
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 6
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 7
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000    // 8
		};
		
		squareEvaluation[PieceType.QUEEN] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 1
			0.000, 0.300, 0.300, 0.300, 0.300, 0.300, 0.300, 0.000,   // 2
			0.000, 0.300, 0.700, 0.700, 0.700, 0.700, 0.300, 0.000,   // 3
			0.000, 0.300, 0.700, 1.000, 1.000, 0.700, 0.300, 0.000,   // 4
			0.000, 0.300, 0.700, 1.000, 1.000, 0.700, 0.300, 0.000,   // 5
			0.000, 0.300, 0.700, 0.700, 0.700, 0.700, 0.300, 0.000,   // 6
			0.000, 0.300, 0.300, 0.300, 0.300, 0.300, 0.300, 0.000,   // 7
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000    // 8
		};
		
		squareEvaluation[PieceType.KING] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 1
			0.000, 0.300, 0.300, 0.300, 0.300, 0.300, 0.300, 0.000,   // 2
			0.000, 0.300, 0.700, 0.700, 0.700, 0.700, 0.300, 0.000,   // 3
			0.000, 0.300, 0.700, 1.000, 1.000, 0.700, 0.300, 0.000,   // 4
			0.000, 0.300, 0.700, 1.000, 1.000, 0.700, 0.300, 0.000,   // 5
			0.000, 0.300, 0.700, 0.700, 0.700, 0.700, 0.300, 0.000,   // 6
			0.000, 0.300, 0.300, 0.300, 0.300, 0.300, 0.300, 0.000,   // 7
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000    // 8
		};
		
		final double[] pieceTypeCoeffs = new double[PieceType.LAST];
		
		pieceTypeCoeffs[PieceType.PAWN] = 0.8;
		pieceTypeCoeffs[PieceType.KNIGHT] = 0.4;
		pieceTypeCoeffs[PieceType.BISHOP] = 0.3;
		pieceTypeCoeffs[PieceType.ROOK] = 0.1;
		pieceTypeCoeffs[PieceType.QUEEN] = 0.3;
		pieceTypeCoeffs[PieceType.KING] = 0.4;
		
		tableSettings.setPieceEvaluationTable(squareEvaluation, pieceTypeCoeffs);
	}
	
	private void setDefaultPawnTables() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int pawnEvaluation = PieceTypeEvaluations.getPieceEvaluation(color, PieceType.PAWN);
			ruleOfSquareBonus[color] = (int) Math.round(pawnEvaluation * RULE_OF_SQUARE_BONUS);
		}
	}
	
	public int getRuleOfSquareBonus(final int color) {
		return ruleOfSquareBonus[color];
	}

	public PawnStructureEvaluatorSettings getPawnStructureEvaluatorSettings() {
		return pawnStructureEvaluatorSettings;
	}
	
}
