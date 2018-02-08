package bishop.engine;

import java.util.Arrays;

import bishop.base.Color;
import bishop.base.File;
import bishop.base.Rank;
import bishop.base.Square;

public class PawnStructureFeatures {
	
	private final int firstFeature;
	
	private final int featureUnprotectedOpenFilePawnBonus;
	private final int[][] connectedPassedPawnBonusFeatures;
	private final int[][] protectedPassedPawnBonusFeatures;
	private final int[][] singlePassedPawnBonusFeatures;
	private final int[][] rookPawnBonusFeatures;
	private final int[][] doublePawnBonusFeatures;
	private final int[][] connectedNotPassedPawnBonusFeatures;
	private final int[][] protectedNotPassedPawnBonusFeatures;
	private final int[][] singleDisadvantageAttackPawnBonusFeatures;
	private final int[][] doubleDisadvantageAttackPawnBonusFeatures;
	private final int[][] blockedPawnBonusFeatures;
	private final int[] pawnMajorityFeatures;
	private final int[] outsidePassedPawnBonusFeatures;
	
	private final int lastFeature;
	
	private static final int PAWN_COUNT = 8;
		
	
	public PawnStructureFeatures(final FeatureRegistry registry, final boolean withFigures) {
		firstFeature = registry.enterCategory("pawn_structure");
		
		if (withFigures) {
			featureUnprotectedOpenFilePawnBonus = registry.add("unprotected_open_file_pawn");
			rookPawnBonusFeatures = createRankFeatures (registry, "rook_pawn", Rank.R2, Rank.R7);		
		}
		else {
			// Without figures -> without coeffs that requires figure
			featureUnprotectedOpenFilePawnBonus = -1;
			rookPawnBonusFeatures = createRankFeatures (registry, "rook_pawn", Rank.LAST, Rank.FIRST);
		}
		
		connectedPassedPawnBonusFeatures = createRankFeatures (registry, "connected_passed_pawn", Rank.R2, Rank.R7);
		protectedPassedPawnBonusFeatures = createRankFeatures (registry, "protected_passed_pawn", Rank.R3, Rank.R7);
		singlePassedPawnBonusFeatures = createRankFeatures (registry, "single_passed_pawn", Rank.R2, Rank.R7);
		doublePawnBonusFeatures = createRankFeatures (registry, "double_pawn", Rank.R2, Rank.R6);
		connectedNotPassedPawnBonusFeatures = createRankFeatures (registry, "connected_not_passed_pawn", Rank.R2, Rank.R6);
		protectedNotPassedPawnBonusFeatures = createRankFeatures (registry, "protected_not_passed_pawn", Rank.R3, Rank.R6);
		singleDisadvantageAttackPawnBonusFeatures = createRankFeatures (registry, "single_disadvantage_attack_pawn", Rank.R2, Rank.R6);
		doubleDisadvantageAttackPawnBonusFeatures = createRankFeatures (registry, "double_disadvantage_attack_pawn", Rank.R2, Rank.R6);
		blockedPawnBonusFeatures = createRankFeatures (registry, "blocked_pawn", Rank.R2, Rank.R6);
		pawnMajorityFeatures = createPawnMajorityFeatures (registry);
		outsidePassedPawnBonusFeatures = createOutsidePassedPawnBonusFeatures(registry);
		
		lastFeature = registry.leaveCategory();
	}
	
	private int[][] createRankFeatures(final FeatureRegistry registry, final String category, final int from, final int to) {
		registry.enterCategory(category);
		
		final int[][] features = new int[Color.LAST][Rank.LAST];
		
		for (int rank = Rank.FIRST; rank < Rank.LAST; rank++) {
			final int featureIndex = (rank >= from && rank <= to) ?
					registry.add(Character.toString(Rank.toChar(rank))) :
					-1;
			
			features[Color.WHITE][rank] = featureIndex;
			features[Color.BLACK][Rank.getOppositeRank(rank)] = featureIndex;
		}
		
		registry.leaveCategory();
		
		return features;
	}
	
	private int[] createPawnMajorityFeatures(final FeatureRegistry registry) {
		final int[] features = new int[Square.COUNT];
		
		registry.enterCategory("pawn_majority");
		
		for (int i = 0; i < PAWN_COUNT; i++)
			features[i] = registry.add(Integer.toString(i));
		
		Arrays.fill(features, PAWN_COUNT, Square.COUNT, features[PAWN_COUNT - 1]);
		
		registry.leaveCategory();
		
		return features;
	}

	private int[] createOutsidePassedPawnBonusFeatures(final FeatureRegistry registry) {
		registry.enterCategory("outside_passed_pawn");
		
		final int maxDistance = File.LAST + 1;
		final int[] features = new int[maxDistance];
		
		for (int distance = 0; distance < maxDistance; distance++) {
			final int coeffIndex = registry.add(Integer.toString(distance));
			
			features[distance] = coeffIndex;
		}
		
		registry.leaveCategory();
		
		return features;
	}

	public int getFeatureUnprotectedOpenFilePawnBonus() {
		return featureUnprotectedOpenFilePawnBonus;
	}


	public int getConnectedPassedPawnBonusFeature(final int color, final int rank) {
		return connectedPassedPawnBonusFeatures[color][rank];
	}


	public int getProtectedPassedPawnBonusFeature(final int color, final int rank) {
		return protectedPassedPawnBonusFeatures[color][rank];
	}


	public int getSinglePassedPawnBonusFeature(final int color, final int rank) {
		return singlePassedPawnBonusFeatures[color][rank];
	}


	public int getRookPawnBonusFeature(final int color, final int rank) {
		return rookPawnBonusFeatures[color][rank];
	}


	public int getDoublePawnBonusFeature(final int color, final int rank) {
		return doublePawnBonusFeatures[color][rank];
	}
	
	public int getConnectedNotPassedPawnBonusFeature(final int color, final int rank) {
		return connectedNotPassedPawnBonusFeatures[color][rank];
	}


	public int getProtectedNotPassedPawnBonusFeature(final int color, final int rank) {
		return protectedNotPassedPawnBonusFeatures[color][rank];
	}
	
	public int getSingleDisadvantageAttackPawnBonusFeature(int color, int rank) {
		return singleDisadvantageAttackPawnBonusFeatures[color][rank];
	}

	public int getDoubleDisadvantageAttackPawnBonusFeature(int color, int rank) {
		return doubleDisadvantageAttackPawnBonusFeatures[color][rank];
	}

	public int getBlockedPawnBonusFeature(int color, int rank) {
		return blockedPawnBonusFeatures[color][rank];
	}

	public int getPawnMajorityFeature(final int minPieceCount) {
		return pawnMajorityFeatures[minPieceCount];
	}
	
	public int getOutsidePassedPawnBonusFeature(final int minOppositePawnFileDistance) {
		return outsidePassedPawnBonusFeatures[minOppositePawnFileDistance];
	}
	
	public int getFirstFeature() {
		return firstFeature;
	}
	
	public int getLastFeature() {
		return lastFeature;
	}

}
