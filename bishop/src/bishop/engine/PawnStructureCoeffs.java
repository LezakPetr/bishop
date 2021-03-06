package bishop.engine;

import java.util.Arrays;

import bishop.base.Color;
import bishop.base.File;
import bishop.base.Rank;
import bishop.base.Square;
import math.IVector;
import math.Vectors;

public class PawnStructureCoeffs {
	
	private final int firstCoeff;
	
	private final int coeffUnprotectedOpenFilePawnBonus;
	private final int[][] connectedPassedPawnBonusCoeffs;
	private final int[][] protectedPassedPawnBonusCoeffs;
	private final int[][] singlePassedPawnBonusCoeffs;
	private final int[][] doublePawnBonusCoeffs;
	private final int[][] connectedNotPassedPawnBonusCoeffs;
	private final int[][] protectedNotPassedPawnBonusCoeffs;
	private final int[][] singleDisadvantageAttackPawnBonusCoeffs;
	private final int[][] doubleDisadvantageAttackPawnBonusCoeffs;
	private final int[][] blockedPawnBonusCoeffs;
	private final int[] pawnMajorityCoeffs;
	private final int[] outsidePassedPawnBonusCoeffs;
	
	private final int lastCoeff;
	
	private static final int PAWN_COUNT = 8;
	private static final int RANK_FEATURE_COUNT = 4;
		
	
	public PawnStructureCoeffs(final CoeffRegistry registry) {
		firstCoeff = registry.enterCategory("pawn_structure");

		coeffUnprotectedOpenFilePawnBonus = registry.addCoeff("unprotected_open_file_pawn");

		connectedPassedPawnBonusCoeffs = createRankCoeffs (registry, "connected_passed_pawn", Rank.R2, Rank.R7);
		protectedPassedPawnBonusCoeffs = createRankCoeffs (registry, "protected_passed_pawn", Rank.R3, Rank.R7);
		singlePassedPawnBonusCoeffs = createRankCoeffs (registry, "single_passed_pawn", Rank.R2, Rank.R7);
		doublePawnBonusCoeffs = createRankCoeffs (registry, "double_pawn", Rank.R2, Rank.R6);
		connectedNotPassedPawnBonusCoeffs = createRankCoeffs (registry, "connected_not_passed_pawn", Rank.R2, Rank.R6);
		protectedNotPassedPawnBonusCoeffs = createRankCoeffs (registry, "protected_not_passed_pawn", Rank.R3, Rank.R6);
		singleDisadvantageAttackPawnBonusCoeffs = createRankCoeffs (registry, "single_disadvantage_attack_pawn", Rank.R2, Rank.R6);
		doubleDisadvantageAttackPawnBonusCoeffs = createRankCoeffs (registry, "double_disadvantage_attack_pawn", Rank.R2, Rank.R6);
		blockedPawnBonusCoeffs = createRankCoeffs (registry, "blocked_pawn", Rank.R2, Rank.R6);
		pawnMajorityCoeffs = createPawnMajorityCoeffs (registry);
		outsidePassedPawnBonusCoeffs = createOutsidePassedPawnBonusCoeffs(registry);
		
		lastCoeff = registry.leaveCategory();
	}
	
	private int[][] createRankCoeffs(final CoeffRegistry registry, final String category, final int from, final int to) {
		registry.enterCategory(category);
		
		final int[][] coeffs = new int[Color.LAST][Rank.LAST];

		// Features will be coefficients of polynomial with zero on the beginning rank
		final int[] featureIndices = new int[RANK_FEATURE_COUNT];

		for (int i = 0; i < RANK_FEATURE_COUNT; i++)
				featureIndices[i] = registry.addFeature();
		
		for (int rank = Rank.FIRST; rank < Rank.LAST; rank++) {
			final int coeffIndex;

			if (rank >= from && rank <= to) {
				final double t = (double) (rank - from) / (double) (to - from);
				final IVector features = Vectors.sparse(registry.getFeatureCount());

				for (int i = 0; i < RANK_FEATURE_COUNT; i++)
					features.setElement(featureIndices[i], Math.pow(t, i));

				coeffIndex = registry.addCoeffWithFeatures(
						Character.toString(Rank.toChar(rank)),
						features.freeze()
				);
			}
			else
				coeffIndex = -1;
			
			coeffs[Color.WHITE][rank] = coeffIndex;
			coeffs[Color.BLACK][Rank.getOppositeRank(rank)] = coeffIndex;
		}

		registry.leaveCategory();
		
		return coeffs;
	}
	
	private int[] createPawnMajorityCoeffs(final CoeffRegistry registry) {
		final int[] coeffs = new int[Square.COUNT];
		
		registry.enterCategory("pawn_majority");
		
		for (int i = 0; i < PAWN_COUNT; i++)
			coeffs[i] = registry.addCoeff(Integer.toString(i));

		Arrays.fill(coeffs, PAWN_COUNT, Square.COUNT, coeffs[PAWN_COUNT - 1]);
		
		registry.leaveCategory();
		
		return coeffs;
	}

	private int[] createOutsidePassedPawnBonusCoeffs(final CoeffRegistry registry) {
		registry.enterCategory("outside_passed_pawn");
		
		final int maxDistance = File.LAST + 1;
		final int[] coeffs = new int[maxDistance];
		
		for (int distance = 0; distance < maxDistance; distance++) {
			final int coeffIndex = registry.addCoeff(Integer.toString(distance));
			
			coeffs[distance] = coeffIndex;
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


	public int getProtectedPassedPawnBonusCoeff(final int color, final int rank) {
		return protectedPassedPawnBonusCoeffs[color][rank];
	}


	public int getSinglePassedPawnBonusCoeff(final int color, final int rank) {
		return singlePassedPawnBonusCoeffs[color][rank];
	}

	public int getDoublePawnBonusCoeff(final int color, final int rank) {
		return doublePawnBonusCoeffs[color][rank];
	}
	
	public int getConnectedNotPassedPawnBonusCoeff(final int color, final int rank) {
		return connectedNotPassedPawnBonusCoeffs[color][rank];
	}


	public int getProtectedNotPassedPawnBonusCoeff(final int color, final int rank) {
		return protectedNotPassedPawnBonusCoeffs[color][rank];
	}
	
	public int getSingleDisadvantageAttackPawnBonusCoeff(int color, int rank) {
		return singleDisadvantageAttackPawnBonusCoeffs[color][rank];
	}

	public int getDoubleDisadvantageAttackPawnBonusCoeff(int color, int rank) {
		return doubleDisadvantageAttackPawnBonusCoeffs[color][rank];
	}

	public int getBlockedPawnBonusCoeff(int color, int rank) {
		return blockedPawnBonusCoeffs[color][rank];
	}

	public int getPawnMajorityCoeff(final int minPieceCount) {
		return pawnMajorityCoeffs[minPieceCount];
	}
	
	public int getOutsidePassedPawnBonusCoeff(final int minOppositePawnFileDistance) {
		return outsidePassedPawnBonusCoeffs[minOppositePawnFileDistance];
	}
	
	public int getFirstCoeff() {
		return firstCoeff;
	}
	
	public int getLastCoeff() {
		return lastCoeff;
	}

}
