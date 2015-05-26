package bishop.engine;

import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Rank;
import bishop.base.Square;

public final class EndingPositionEvaluatorSettings {

	private final TablePositionEvaluatorSettings tableSettings;
	
	private final int[][] singlePassedPawnBonus;
	private final int[][] protectedPassedPawnBonus;
	private final int[][] connectedPassedPawnBonus;
	private final int[] doublePawnBonus;
	private final int[] ruleOfSquareBonus;
	private final int[][] rookPawnBonus;
	
	private final static double SINGLE_PASSED_PAWN_COEFF = 0.050;
	private final static double SINGLE_PASSED_PAWN_ROOT = 2.0;
	
	private final static double PROTECTED_PASSED_PAWN_COEFF = 0.100;
	private final static double PROTECTED_PASSED_PAWN_ROOT = 2.0;
	
	private final static double CONNECTED_PASSED_PAWN_COEFF = 0.075;   // For each connected pawn
	private final static double CONNECTED_PASSED_PAWN_ROOT = 2.0;
	
	private final static double DOUBLE_PAWN_BONUS = -0.4;
	private final static double RULE_OF_SQUARE_BONUS = 5.0;
	
	private final static double MAX_ROOK_PAWN_BONUS = 1.0;

	
	public EndingPositionEvaluatorSettings() {
		tableSettings = new TablePositionEvaluatorSettings();
		
		singlePassedPawnBonus = new int[Color.LAST][Square.LAST];
		protectedPassedPawnBonus = new int[Color.LAST][Square.LAST];
		connectedPassedPawnBonus = new int[Color.LAST][Square.LAST];
		doublePawnBonus = new int[Color.LAST];
		ruleOfSquareBonus = new int[Color.LAST];
		rookPawnBonus = new int[Color.LAST][Rank.LAST];

		setDefaultTables();
		setDefaultPawnTables();
		setDefaultRookPawnBonus();
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
			
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				final int promotionDistance = BoardConstants.getPawnPromotionDistance(color, square);
				final int exponent = 5 - promotionDistance;
				
				final double singleBonus = SINGLE_PASSED_PAWN_COEFF * Math.pow(SINGLE_PASSED_PAWN_ROOT, exponent);
				singlePassedPawnBonus[color][square] = (int) Math.round (pawnEvaluation * singleBonus);

				final double protectedBonus = PROTECTED_PASSED_PAWN_COEFF * Math.pow(PROTECTED_PASSED_PAWN_ROOT, exponent);
				protectedPassedPawnBonus[color][square] = (int) Math.round (pawnEvaluation * protectedBonus);

				final double connectedBonus = CONNECTED_PASSED_PAWN_COEFF * Math.pow(CONNECTED_PASSED_PAWN_ROOT, exponent);
				connectedPassedPawnBonus[color][square] = (int) Math.round (pawnEvaluation * connectedBonus);
			}
			
			doublePawnBonus[color] = (int) Math.round(pawnEvaluation * DOUBLE_PAWN_BONUS);
			ruleOfSquareBonus[color] = (int) Math.round(pawnEvaluation * RULE_OF_SQUARE_BONUS);
		}
	}
	
	private void setDefaultRookPawnBonus() {
		final double maxDistance = Rank.R7 - Rank.R2;
		final double coeff = MAX_ROOK_PAWN_BONUS / maxDistance;
		
		for (int rank = Rank.FIRST; rank < Rank.LAST; rank++) {
			final double bonus = coeff * (rank - Rank.R2);
			final int evaluation = (int) Math.round(bonus * PieceTypeEvaluations.PAWN_EVALUATION);;
			
			rookPawnBonus[Color.WHITE][rank] = evaluation;
			rookPawnBonus[Color.BLACK][Rank.getOppositeRank(rank)] = evaluation;
		}
	}

	public int getConnectedPassedPawnBonus(final int color, final int square) {
		return connectedPassedPawnBonus[color][square];
	}

	public int getProtectedPassedPawnBonus(final int color, final int square) {
		return protectedPassedPawnBonus[color][square];
	}

	public int getSinglePassedPawnBonus(final int color, final int square) {
		return singlePassedPawnBonus[color][square];
	}

	public int getRookPawnBonus(final int color, final int rank) {
		return rookPawnBonus[color][rank];
	}

	public int getDoublePawnBonus(final int color) {
		return doublePawnBonus[color];
	}

	public int getRuleOfSquareBonus(final int color) {
		return ruleOfSquareBonus[color];
	}
	
}
