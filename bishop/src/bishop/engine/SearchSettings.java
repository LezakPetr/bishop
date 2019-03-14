package bishop.engine;


import static math.Utils.roundToInt;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import bishop.base.PieceTypeEvaluations;


public final class SearchSettings {

	public static final int EXTENSION_FRACTION_BITS = 10;
	public static final int EXTENSION_GRANULARITY = 1 << EXTENSION_FRACTION_BITS;
	public static final int EXTENSION_TRESHOLD = makeExtension(0.8);

	public static final String CSV_HEADER = "maxQuiescenceDepth, maxCheckSearchDepth, nullMoveReduction, minExtensionHorizon, " +
			"simpleCheckExtension, attackCheckExtension, forcedMoveExtension, mateExtension, rankAttackExtension, " +
			"pawnOnSevenRankExtension, protectingPawnOnSixRankExtension, " +
			"recaptureMinExtension, recaptureMaxExtension, " +
			"recaptureBeginMinTreshold, recaptureBeginMaxTreshold, recaptureTargetTreshold, maxEstimateForZeroWindowSearch";

	private int maxQuiescenceDepth;
	private int nullMoveReduction;
	private int minExtensionHorizon;
	private int simpleCheckExtension;
	private int attackCheckExtension;
	private int forcedMoveExtension;
	private int mateExtension;
	private int rankAttackExtension;

	private int pawnOnSevenRankExtension;
	private int protectingPawnOnSixRankExtension;

	private int recaptureMinExtension;
	private int recaptureMaxExtension;
	private int recaptureBeginMinTreshold;
	private int recaptureBeginMaxTreshold;
	private int recaptureTargetTreshold;
	private int maxCheckSearchDepth;
	private int maxEstimateForZeroWindowSearch;

	public SearchSettings() {
		maxQuiescenceDepth = 13;
		maxCheckSearchDepth = 11;
		nullMoveReduction = 4;
		minExtensionHorizon = 2;

		simpleCheckExtension = makeExtension(0.0);
		attackCheckExtension = makeExtension(0.7509766);
		forcedMoveExtension = makeExtension(0.6708984);
		mateExtension = makeExtension(0.5);
		rankAttackExtension = makeExtension(0.9521484);
		
		pawnOnSevenRankExtension = makeExtension(0.9853516);
		protectingPawnOnSixRankExtension = makeExtension(1.0);
		
		recaptureMinExtension = makeExtension(0.171875);
		recaptureMaxExtension = makeExtension(0.9580078);

		recaptureBeginMinTreshold = roundToInt (7.381 * PieceTypeEvaluations.PAWN_EVALUATION);
		recaptureBeginMaxTreshold = roundToInt (8.317 * PieceTypeEvaluations.PAWN_EVALUATION);
		recaptureTargetTreshold = roundToInt (6.029 * PieceTypeEvaluations.PAWN_EVALUATION);
		maxEstimateForZeroWindowSearch = -126200;
	}
	
	private static int makeExtension(final double extension) {
		return roundToInt(extension * EXTENSION_GRANULARITY);
	}
	
	public int getMaxQuiescenceDepth() {
		return maxQuiescenceDepth;
	}
	
	public void setMaxQuiescenceDepth(final int maxQuiescenceDepth) {
		this.maxQuiescenceDepth = maxQuiescenceDepth;
	}

	public int getNullMoveReduction() {
		return nullMoveReduction;
	}

	public void setNullMoveReduction(int nullMoveReduction) {
		this.nullMoveReduction = nullMoveReduction;
	}

	public int getMinExtensionHorizon() {
		return minExtensionHorizon;
	}

	public void setMinExtensionHorizon(final int minExtensionHorizon) {
		this.minExtensionHorizon = minExtensionHorizon;
	}

	public int getSimpleCheckExtension() {
		return simpleCheckExtension;
	}

	public void setSimpleCheckExtension(int simpleCheckExtension) {
		this.simpleCheckExtension = simpleCheckExtension;
	}

	public int getAttackCheckExtension() {
		return attackCheckExtension;
	}

	public void setAttackCheckExtension(int attackCheckExtension) {
		this.attackCheckExtension = attackCheckExtension;
	}

	public int getForcedMoveExtension() {
		return forcedMoveExtension;
	}

	public void setForcedMoveExtension(int forcedMoveExtension) {
		this.forcedMoveExtension = forcedMoveExtension;
	}

	public int getMateExtension() {
		return mateExtension;
	}

	public void setMateExtension(int mateExtension) {
		this.mateExtension = mateExtension;
	}

	public int getRankAttackExtension() {
		return rankAttackExtension;
	}

	public void setRankAttackExtension(int rankAttackExtension) {
		this.rankAttackExtension = rankAttackExtension;
	}

	public int getPawnOnSevenRankExtension() {
		return pawnOnSevenRankExtension;
	}

	public void setPawnOnSevenRankExtension(int pawnOnSevenRankExtension) {
		this.pawnOnSevenRankExtension = pawnOnSevenRankExtension;
	}

	public int getRecaptureMinExtension() {
		return recaptureMinExtension;
	}

	public void setRecaptureMinExtension(int recaptureMinExtension) {
		this.recaptureMinExtension = recaptureMinExtension;
	}

	public int getRecaptureMaxExtension() {
		return recaptureMaxExtension;
	}

	public void setRecaptureMaxExtension(int recaptureMaxExtension) {
		this.recaptureMaxExtension = recaptureMaxExtension;
	}

	public int getRecaptureBeginMinTreshold() {
		return recaptureBeginMinTreshold;
	}

	public void setRecaptureBeginMinTreshold(int recaptureBeginMinTreshold) {
		this.recaptureBeginMinTreshold = recaptureBeginMinTreshold;
	}

	public int getRecaptureBeginMaxTreshold() {
		return recaptureBeginMaxTreshold;
	}

	public void setRecaptureBeginMaxTreshold(int recaptureBeginMaxTreshold) {
		this.recaptureBeginMaxTreshold = recaptureBeginMaxTreshold;
	}

	public int getRecaptureTargetTreshold() {
		return recaptureTargetTreshold;
	}

	public void setRecaptureTargetTreshold(int recaptureTargetTreshold) {
		this.recaptureTargetTreshold = recaptureTargetTreshold;
	}

	public int getMaxEstimateForZeroWindowSearch() {
		return maxEstimateForZeroWindowSearch;
	}

	public void setMaxEstimateForZeroWindowSearch(int maxEstimateForZeroWindowSearch) {
		this.maxEstimateForZeroWindowSearch = maxEstimateForZeroWindowSearch;
	}

	public void assign(final SearchSettings orig) {
		maxQuiescenceDepth = orig.maxQuiescenceDepth;
		maxCheckSearchDepth = orig.maxCheckSearchDepth;
		nullMoveReduction = orig.nullMoveReduction;
		minExtensionHorizon = orig.minExtensionHorizon;

		simpleCheckExtension = orig.simpleCheckExtension;
		attackCheckExtension = orig.attackCheckExtension;
		forcedMoveExtension = orig.forcedMoveExtension;
		mateExtension = orig.mateExtension;
		rankAttackExtension = orig.rankAttackExtension;

		pawnOnSevenRankExtension = orig.pawnOnSevenRankExtension;
		protectingPawnOnSixRankExtension = orig.protectingPawnOnSixRankExtension;

		recaptureMinExtension = orig.recaptureMinExtension;
		recaptureMaxExtension = orig.recaptureMaxExtension;

		recaptureBeginMinTreshold = orig.recaptureBeginMinTreshold;
		recaptureBeginMaxTreshold = orig.recaptureBeginMaxTreshold;
		recaptureTargetTreshold = orig.recaptureTargetTreshold;
	}

	private static void printExtension(final PrintWriter writer, final int value) {
		final double relativeValue = (double) value / (double) EXTENSION_GRANULARITY;
		
		writer.print(relativeValue + ", ");
	}

	private static void printRelativeEvaluation (final PrintWriter writer, final int value) {
		printRelativeEvaluation(writer, value, true);
	}

	private static void printRelativeEvaluation (final PrintWriter writer, final int value, final boolean withComma) {
		final double relativeValue = (double) value / (double) PieceTypeEvaluations.PAWN_EVALUATION;
		
		writer.print(relativeValue);

		if (withComma)
			writer.print(", ");
	}

	@Override
	public String toString() {
		try (
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
		)
		{
			printWriter.print(maxQuiescenceDepth + ", ");
			printWriter.print(maxCheckSearchDepth + ", ");
			printWriter.print(nullMoveReduction + ", ");
			printWriter.print(minExtensionHorizon + ", ");

			printExtension(printWriter, simpleCheckExtension);
			printExtension(printWriter, attackCheckExtension);
			printExtension(printWriter, forcedMoveExtension);
			printExtension(printWriter, mateExtension);
			printExtension(printWriter, rankAttackExtension);
			
			printExtension(printWriter, pawnOnSevenRankExtension);
			printExtension(printWriter, protectingPawnOnSixRankExtension);
			
			printExtension(printWriter, recaptureMinExtension);
			printExtension(printWriter, recaptureMaxExtension);

			printRelativeEvaluation(printWriter, recaptureBeginMinTreshold);
			printRelativeEvaluation(printWriter, recaptureBeginMaxTreshold);
			printRelativeEvaluation(printWriter, recaptureTargetTreshold);

			printWriter.print(maxEstimateForZeroWindowSearch);

			printWriter.flush();
			return stringWriter.toString();
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot print SearchSettings", ex);
		}
	}

	public int getProtectingPawnOnSixRankExtension() {
		return protectingPawnOnSixRankExtension;
	}

	public void setProtectingPawnOnSixRankExtension(int protectingPawnOnSixRankExtension) {
		this.protectingPawnOnSixRankExtension = protectingPawnOnSixRankExtension;
	}

	public int getMaxCheckSearchDepth() {
		return maxCheckSearchDepth;
	}

	public void setMaxCheckSearchDepth(final int maxCheckSearchDepth) {
		this.maxCheckSearchDepth =  maxCheckSearchDepth;
	}

}
