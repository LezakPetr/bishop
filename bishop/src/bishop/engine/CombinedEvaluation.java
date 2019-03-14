package bishop.engine;

import math.Utils;

import java.util.stream.IntStream;

public class CombinedEvaluation {

	public static final int COMPONENT_FIRST = 0;
	public static final int COMPONENT_ENDING = 0;
	public static final int COMPONENT_MIDDLE_GAME = 1;
	public static final int COMPONENT_OPENING = 2;
	public static final int COMPONENT_LAST = 3;

	public static final int ALPHA_BITS = 7;
	public static final int MAX_ALPHA = 1 << ALPHA_BITS;

	public static final int MIDDLE_GAME_TRESHOLD = 8;

	private static final int COMPONENT_SHIFT = Evaluation.BITS + 1;

	private static final int ENDING_SHIFT = COMPONENT_ENDING * COMPONENT_SHIFT;
	private static final int MIDDLE_GAME_SHIFT = COMPONENT_MIDDLE_GAME * COMPONENT_SHIFT;
	private static final int OPENING_SHIFT = COMPONENT_OPENING * COMPONENT_SHIFT;

	private static final int EVALUATION_MASK = (1 << Evaluation.BITS) - 1;

	private static final int BASE_EXPONENT = Evaluation.BITS - 1;

	private static final int EVALUATION_BASE = 1 << BASE_EXPONENT;

	public static final long ACCUMULATOR_BASE =
			(1L << (BASE_EXPONENT + ENDING_SHIFT)) +
			(1L << (BASE_EXPONENT + MIDDLE_GAME_SHIFT)) +
			(1L << (BASE_EXPONENT + OPENING_SHIFT));

	private static final long[] EVALUATION_DECODE_MULTIPLICATORS = IntStream.range(GameStage.FIRST, GameStage.LAST)
			.mapToLong(CombinedEvaluation::calculateMultiplicatorForGameStage)
			.toArray();

	public static long calculateMultiplicatorForGameStage(final int gameStage) {
		int lowerShift;
		int upperShift;
		double t;

		if (gameStage <= MIDDLE_GAME_TRESHOLD) {
			t = (double) (gameStage - GameStage.FIRST) / (double) (MIDDLE_GAME_TRESHOLD - GameStage.FIRST);
			lowerShift = ENDING_SHIFT;
			upperShift = MIDDLE_GAME_SHIFT;
		}
		else {
			t = (double) (gameStage - MIDDLE_GAME_TRESHOLD) / (double) (GameStage.LAST - 1 - MIDDLE_GAME_TRESHOLD);
			lowerShift = MIDDLE_GAME_SHIFT;
			upperShift = OPENING_SHIFT;
		}

		final long alpha = Utils.roundToInt(MAX_ALPHA * t);

		return ((MAX_ALPHA - alpha) << lowerShift) + (alpha << upperShift);
	}


	public static int decode (final long combinedEvaluation, final long multiplicator) {
		final int opening = (((int) (combinedEvaluation >>> OPENING_SHIFT) & EVALUATION_MASK) - EVALUATION_BASE) * ((int) (multiplicator >>> OPENING_SHIFT) & EVALUATION_MASK);
		final int middleGame = (((int) (combinedEvaluation >>> MIDDLE_GAME_SHIFT) & EVALUATION_MASK) - EVALUATION_BASE) * ((int) (multiplicator >>> MIDDLE_GAME_SHIFT) & EVALUATION_MASK);
		final int ending = (((int) (combinedEvaluation >>> ENDING_SHIFT) & EVALUATION_MASK) - EVALUATION_BASE) * ((int) (multiplicator >>> ENDING_SHIFT) & EVALUATION_MASK);

		return (opening + middleGame + ending) >> ALPHA_BITS;
	}

	public static long getMultiplicatorForGameStage (final int gameStage) {
		return EVALUATION_DECODE_MULTIPLICATORS[gameStage];
	}

	public static long combine (final int evaluationOpening, final int evaluationMiddle, final int evaluationEnding) {
		return ((long) evaluationOpening << OPENING_SHIFT) +
		       ((long) evaluationMiddle << MIDDLE_GAME_SHIFT) +
				((long) evaluationEnding << ENDING_SHIFT);
	}

	public static int getComponentMultiplicator (final int gameStage, final int component) {
		return (int) (getMultiplicatorForGameStage(gameStage) >>> (component * COMPONENT_SHIFT)) & EVALUATION_MASK;
	}
}
