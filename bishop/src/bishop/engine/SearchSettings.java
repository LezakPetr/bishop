package bishop.engine;


import static math.Utils.roundToInt;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import utils.DoubleArrayBuilder;


public final class SearchSettings {
	
	private int maxQuiescenceDepth;
	private int nullMoveReduction;
	private int minExtensionHorizon;
	private int maxExtension;
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
	private int pinExtension;
	private int maxCheckSearchDepth;

	public SearchSettings() {
		maxQuiescenceDepth = makeHorizon(5.0);
		nullMoveReduction = makeHorizon(3.0);
		minExtensionHorizon = makeHorizon(3.0);
		maxExtension = makeHorizon(5.0);
		simpleCheckExtension = makeHorizon(0.5);
		attackCheckExtension = makeHorizon(1.0);
		forcedMoveExtension = makeHorizon(0.75);
		mateExtension = makeHorizon(1.0);
		rankAttackExtension = makeHorizon(0.75);
		pinExtension = makeHorizon(0.75);
		
		pawnOnSevenRankExtension = makeHorizon(1.0);
		protectingPawnOnSixRankExtension = makeHorizon(1.0);
		
		recaptureMinExtension = makeHorizon(0.0);
		recaptureMaxExtension = makeHorizon(0.75);

		recaptureBeginMinTreshold = roundToInt (2.25 * PieceTypeEvaluations.PAWN_EVALUATION);
		recaptureBeginMaxTreshold = roundToInt (5 * PieceTypeEvaluations.PAWN_EVALUATION);
		recaptureTargetTreshold = roundToInt (0.5 * PieceTypeEvaluations.PAWN_EVALUATION);
		maxCheckSearchDepth = makeHorizon(4.0);
	}
	
	private static int makeHorizon (final double extension) {
		return roundToInt(extension * ISearchEngine.HORIZON_GRANULARITY);
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

	public int getMaxExtension() {
		return maxExtension;
	}

	public void setMaxExtension(int maxExtension) {
		this.maxExtension = maxExtension;
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
		maxExtension = orig.maxExtension;
		simpleCheckExtension = orig.simpleCheckExtension;
		attackCheckExtension = orig.attackCheckExtension;
		forcedMoveExtension = orig.forcedMoveExtension;
		mateExtension = orig.mateExtension;
		rankAttackExtension = orig.rankAttackExtension;
		pawnOnSevenRankExtension = orig.pawnOnSevenRankExtension;
		recaptureMinExtension = orig.recaptureMinExtension;
		recaptureMaxExtension = orig.recaptureMaxExtension;
		recaptureBeginMinTreshold = orig.recaptureBeginMinTreshold;
		recaptureBeginMaxTreshold = orig.recaptureBeginMaxTreshold;
		recaptureTargetTreshold = orig.recaptureTargetTreshold;
		maxCheckSearchDepth = orig.maxCheckSearchDepth;
	}

	private static void printRelativeHorizon (final PrintWriter writer, final String name, final int value) {
		final double relativeValue = (double) value / (double) ISearchEngine.HORIZON_GRANULARITY;
		
		writer.println(name + " = (int) Math.round (" + relativeValue + " * ISearchEngine.HORIZON_GRANULARITY);");
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
			printRelativeHorizon(printWriter, "maxQuiescenceDepth", maxQuiescenceDepth);
			printRelativeHorizon(printWriter, "nullMoveReduction", nullMoveReduction);
			printRelativeHorizon(printWriter, "minExtensionHorizon", minExtensionHorizon);
			printRelativeHorizon(printWriter, "maxExtension", maxExtension);
			printRelativeHorizon(printWriter, "simpleCheckExtension", simpleCheckExtension);
			printRelativeHorizon(printWriter, "attackCheckExtension", attackCheckExtension);
			printRelativeHorizon(printWriter, "forcedMoveExtension", forcedMoveExtension);
			printRelativeHorizon(printWriter, "mateExtension", mateExtension);
			printRelativeHorizon(printWriter, "rankAttackExtension", rankAttackExtension);
			
			printRelativeHorizon(printWriter, "pawnOnSevenRankExtension", pawnOnSevenRankExtension);
			
			printRelativeHorizon(printWriter, "recaptureMinExtension", recaptureMinExtension);
			printRelativeHorizon(printWriter, "recaptureMaxExtension", recaptureMaxExtension);

			printRelativeEvaluation(printWriter, "recaptureBeginMinTreshold", recaptureBeginMinTreshold);
			printRelativeEvaluation(printWriter, "recaptureBeginMaxTreshold", recaptureBeginMaxTreshold);
			printRelativeEvaluation(printWriter, "recaptureTargetTreshold", recaptureTargetTreshold);

			printRelativeHorizon(printWriter, "maxCheckSearchDepth", maxCheckSearchDepth);
			
			printWriter.flush();
			return stringWriter.toString();
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot print SearchSettings", ex);
		}
	}

	public int getPinExtension() {
		return pinExtension;
	}

	public void setPinExtension(final int pinExtension) {
		this.pinExtension = pinExtension;
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
