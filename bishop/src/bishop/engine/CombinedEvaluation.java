package bishop.engine;

import math.Sigmoid;
import math.Utils;

import java.util.Arrays;
import java.util.stream.IntStream;

public class CombinedEvaluation {
	public
	static final int ALPHA_BITS = 7;
	public static final int MAX_ALPHA = 1 << ALPHA_BITS;

	private static final int COMPONENT_SHIFT = 32;

	private static final int BASE_EXPONENT = 23;
	private static final int DECODE_BASE = 1 << BASE_EXPONENT;
	private static final int DECODE_SHIFT = COMPONENT_SHIFT + ALPHA_BITS;

	public static final long ACCUMULATOR_BASE = (1L << BASE_EXPONENT) + (1L << (BASE_EXPONENT + COMPONENT_SHIFT));

	private static final Sigmoid ALPHA_GAME_STAGE_MAPPING = new Sigmoid(0.3 * GameStage.COUNT, 0.7 * GameStage.COUNT, 0, 1);

	private static final int[] ALPHA_FOR_GAME_STAGES = IntStream.range(GameStage.FIRST, GameStage.LAST)
			.mapToDouble(ALPHA_GAME_STAGE_MAPPING::applyAsDouble)
			.mapToInt(t -> Utils.roundToInt(t * MAX_ALPHA))
			.toArray();

	private static final long[] EVALUATION_DECODE_MULTIPLICATORS = Arrays.stream(ALPHA_FOR_GAME_STAGES)
			.mapToLong(CombinedEvaluation::getMultiplicatorForAlpha)
			.toArray();


	public static int decode (final long combinedEvaluation, final long multiplicator) {
		final long basedEvaluation = (combinedEvaluation * multiplicator) >>> DECODE_SHIFT;
		final int evaluation = (int) basedEvaluation - DECODE_BASE;

		return evaluation;
	}

	public static int getAlphaForGameStage (final int gameStage) {
		return ALPHA_FOR_GAME_STAGES[gameStage];
	}

	public static long getMultiplicatorForAlpha (final int alpha) {
		return (MAX_ALPHA - alpha) + ((long) alpha << COMPONENT_SHIFT);
	}

	public static long getMultiplicatorForGameStage (final int gameStage) {
		return EVALUATION_DECODE_MULTIPLICATORS[gameStage];
	}

	public static long combine (final int evaluationOpening, final int evaluationEnding) {
		return ((long) evaluationOpening << COMPONENT_SHIFT) + (long) evaluationEnding;
	}
}
