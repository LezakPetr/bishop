package bishop.engine;

import bishop.base.Color;
import bishop.base.Rank;

public class PawnStructureCoeffs {
	private final int coeffUnprotectedOpenFilePawnBonus;
	private final int[][] connectedPassedPawnBonusCoeffs;
	private final int[][] protectedPassedPawnBonusCoeffs;
	private final int[][] singlePassedPawnBonusCoeffs;
	private final int[][] rookPawnBonusCoeffs;
	private final int[][] doublePawnBonusCoeffs;
	
	public static final int COEFF_COUNT = 29;
	
	
	public PawnStructureCoeffs(final CoeffRegistry registry, final String category, final boolean withFigures) {
		registry.enterCategory(category);
		
		if (withFigures) {
			coeffUnprotectedOpenFilePawnBonus = registry.add("unprotected_open_file_pawn");
			rookPawnBonusCoeffs = createRankCoeffs (registry, "rook_pawn", Rank.R2, Rank.R7);		
		}
		else {
			// Without figures -> without coeffs that requires figure
			coeffUnprotectedOpenFilePawnBonus = -1;
			rookPawnBonusCoeffs = createRankCoeffs (registry, "rook_pawn", Rank.LAST, Rank.FIRST);
		}
		
		connectedPassedPawnBonusCoeffs = createRankCoeffs (registry, "connected_passed_pawn", Rank.R2, Rank.R7);
		protectedPassedPawnBonusCoeffs = createRankCoeffs (registry, "protected_passed_pawn", Rank.R3, Rank.R7);
		singlePassedPawnBonusCoeffs = createRankCoeffs (registry, "single_passed_pawn", Rank.R2, Rank.R7);
		doublePawnBonusCoeffs = createRankCoeffs (registry, "double_pawn", Rank.R2, Rank.R6);
		
		registry.leaveCategory();
	}
	
	private int[][] createRankCoeffs(final CoeffRegistry registry, final String category, final int from, final int to) {
		registry.enterCategory(category);
		
		final int[][] coeffs = new int[Color.LAST][Rank.LAST];
		
		for (int rank = Rank.FIRST; rank < Rank.LAST; rank++) {
			final int coeffIndex = (rank >= from && rank <= to) ?
					registry.add(Character.toString(Rank.toChar(rank))) :
					-1;
			
			coeffs[Color.WHITE][rank] = coeffIndex;
			coeffs[Color.BLACK][Rank.getOppositeRank(rank)] = coeffIndex;
		}
		
		for (int rank = from; rank < to; rank++) {
			registry.addLink(new CoeffLink(coeffs[Color.WHITE][rank], coeffs[Color.WHITE][rank+1], PositionEvaluationCoeffs.LINK_WEIGHT));
		}
		
		registry.leaveCategory();
		
		return coeffs;
	}


	public int getCoeffUnprotectedOpenFilePawnBonus() {
		return coeffUnprotectedOpenFilePawnBonus;
	}


	public int getConnectedPassedPawnBonusCoeff(final int color, final int rank) {
		return connectedPassedPawnBonusCoeffs[color][rank];
	}


	public int getProtectedPassedPawnBonusCoeffs(final int color, final int rank) {
		return protectedPassedPawnBonusCoeffs[color][rank];
	}


	public int getSinglePassedPawnBonusCoeffs(final int color, final int rank) {
		return singlePassedPawnBonusCoeffs[color][rank];
	}


	public int getRookPawnBonusCoeffs(final int color, final int rank) {
		return rookPawnBonusCoeffs[color][rank];
	}


	public int getDoublePawnBonusCoeffs(final int color, final int rank) {
		return doublePawnBonusCoeffs[color][rank];
	}

}
