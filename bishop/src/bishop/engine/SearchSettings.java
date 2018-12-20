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

	public SearchSettings() {
		maxQuiescenceDepth = 10;
		nullMoveReduction = 4;
		minExtensionHorizon = 6;
		maxCheckSearchDepth = 8;

		simpleCheckExtension = makeExtension(0.5);
		attackCheckExtension = makeExtension(1.0);
		forcedMoveExtension = makeExtension(0.75);
		mateExtension = makeExtension(1.0);
		rankAttackExtension = makeExtension(0.75);
		
		pawnOnSevenRankExtension = makeExtension(1.0);
		protectingPawnOnSixRankExtension = makeExtension(1.0);
		
		recaptureMinExtension = makeExtension(0.0);
		recaptureMaxExtension = makeExtension(0.75);

		recaptureBeginMinTreshold = roundToInt (2.25 * PieceTypeEvaluations.PAWN_EVALUATION);
		recaptureBeginMaxTreshold = roundToInt (5 * PieceTypeEvaluations.PAWN_EVALUATION);
		recaptureTargetTreshold = roundToInt (0.5 * PieceTypeEvaluations.PAWN_EVALUATION);
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

	public void assign(final SearchSettings orig) {
		maxQuiescenceDepth = orig.maxQuiescenceDepth;
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
		maxCheckSearchDepth = orig.maxCheckSearchDepth;
	}

	private static void printExtension(final PrintWriter writer, final String name, final int value) {
		final double relativeValue = (double) value / (double) EXTENSION_GRANULARITY;
		
		writer.println(name + " = makeExtension (" + relativeValue + ");");
	}

	private static void printRelativeEvaluation (final PrintWriter writer, final String name, final int value) {
		final double relativeValue = (double) value / (double) PieceTypeEvaluations.PAWN_EVALUATION;
		
		writer.println(name + " = (int) Math.round (" + relativeValue + " * PieceTypeEvaluations.PAWN_EVALUATION);");
	}

	@Override
	public String toString() {
		try (
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
		)
		{
			printWriter.println("maxQuiescenceDepth = " + maxQuiescenceDepth + ";");
			printWriter.println("nullMoveReduction = " + nullMoveReduction + ";");
			printWriter.println("minExtensionHorizon = " + minExtensionHorizon + ";");

			printExtension(printWriter, "simpleCheckExtension", simpleCheckExtension);
			printExtension(printWriter, "attackCheckExtension", attackCheckExtension);
			printExtension(printWriter, "forcedMoveExtension", forcedMoveExtension);
			printExtension(printWriter, "mateExtension", mateExtension);
			printExtension(printWriter, "rankAttackExtension", rankAttackExtension);
			
			printExtension(printWriter, "pawnOnSevenRankExtension", pawnOnSevenRankExtension);
			printExtension(printWriter, "protectingPawnOnSixRankExtension", protectingPawnOnSixRankExtension);
			
			printExtension(printWriter, "recaptureMinExtension", recaptureMinExtension);
			printExtension(printWriter, "recaptureMaxExtension", recaptureMaxExtension);

			printRelativeEvaluation(printWriter, "recaptureBeginMinTreshold", recaptureBeginMinTreshold);
			printRelativeEvaluation(printWriter, "recaptureBeginMaxTreshold", recaptureBeginMaxTreshold);
			printRelativeEvaluation(printWriter, "recaptureTargetTreshold", recaptureTargetTreshold);

			printWriter.println("maxCheckSearchDepth = " + maxCheckSearchDepth + ";");
			
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

	public int getMaxCheckSearchDepth() {
		return maxCheckSearchDepth;
	}

	public void setMaxCheckSearchDepth(final int maxCheckSearchDepth) {
		this.maxCheckSearchDepth =  maxCheckSearchDepth;
	}

}
