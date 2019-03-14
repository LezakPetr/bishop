package bishop.engine;

import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Rank;
import bishop.base.Square;

public class PawnStructureEvaluatorSettings {
	private final int[][] singlePassedPawnBonus;
	private final int[][] protectedPassedPawnBonus;
	private final int[][] connectedPassedPawnBonus;

	private final int[] unprotectedOpenFilePawnBonus;
	private final int[] doublePawnBonus;
	private final int[][] rookPawnBonus;

	
	private static final double UNPROTECTED_OPEN_FILE_PAWN_BONUS = -0.2;
	private static final double DOUBLE_PAWN_BONUS = -0.3;
	
	private final static double SINGLE_PASSED_PAWN_COEFF = 0.050;
	private final static double SINGLE_PASSED_PAWN_ROOT = 2.0;
	
	private final static double PROTECTED_PASSED_PAWN_COEFF = 0.100;
	private final static double PROTECTED_PASSED_PAWN_ROOT = 2.0;
	
	private final static double CONNECTED_PASSED_PAWN_COEFF = 0.075;   // For each connected pawn
	private final static double CONNECTED_PASSED_PAWN_ROOT = 2.0;

	private final static double MAX_ROOK_PAWN_BONUS = 1.0;

	
	public PawnStructureEvaluatorSettings() {
		unprotectedOpenFilePawnBonus = new int[Color.LAST];
		doublePawnBonus = new int[Color.LAST];
		singlePassedPawnBonus = new int[Color.LAST][Square.LAST];
		protectedPassedPawnBonus = new int[Color.LAST][Square.LAST];
		connectedPassedPawnBonus = new int[Color.LAST][Square.LAST];
		rookPawnBonus = new int[Color.LAST][Rank.LAST];
		
		setDefaultPawnTables();
		calculateBonusTables();
		setDefaultRookPawnBonus();
	}
	
	private void calculateBonusTables() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int pawnEvaluation = PieceTypeEvaluations.getPawnEvaluation(color);
			
			unprotectedOpenFilePawnBonus[color] = (int) Math.round (pawnEvaluation * UNPROTECTED_OPEN_FILE_PAWN_BONUS);
			doublePawnBonus[color] = (int) Math.round (pawnEvaluation * DOUBLE_PAWN_BONUS);
		}
	}
	
	private void setDefaultPawnTables() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int pawnEvaluation = PieceTypeEvaluations.getPawnEvaluation(color);
			
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

	public int getUnprotectedOpenFilePawnBonus (final int color) {
		return unprotectedOpenFilePawnBonus[color];
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

	public int getDoublePawnBonus(final int color) {
		return doublePawnBonus[color];
	}	

	public int getRookPawnBonus(final int color, final int rank) {
		return rookPawnBonus[color][rank];
	}

}
