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

	public static final int COMPONENT_SHIFT = Evaluation.BITS + 1;

	private static final int ENDING_SHIFT = COMPONENT_ENDING * COMPONENT_SHIFT;
	private static final int MIDDLE_GAME_SHIFT = COMPONENT_MIDDLE_GAME * COMPONENT_SHIFT;
	private static final int OPENING_SHIFT = COMPONENT_OPENING * COMPONENT_SHIFT;

	public static final int EVALUATION_MASK = (1 << Evaluation.BITS) - 1;

	private static final int BASE_EXPONENT = Evaluation.BITS - 1;

	public static final int EVALUATION_BASE = 1 << BASE_EXPONENT;

	public static final long ACCUMULATOR_BASE =
			(1L << (BASE_EXPONENT + ENDING_SHIFT)) +
			(1L << (BASE_EXPONENT + MIDDLE_GAME_SHIFT)) +
			(1L << (BASE_EXPONENT + OPENING_SHIFT));

	private static final CombinedEvaluationDecoder[] COMBINED_EVALUATION_DECODERS = IntStream.range(GameStage.FIRST, GameStage.LAST)
			.mapToObj(CombinedEvaluation::calculateDecoderForGameStage)
			.toArray(CombinedEvaluationDecoder[]::new);

	public static CombinedEvaluationDecoder calculateDecoderForGameStage(final int gameStage) {
		int shift;
		double t;

		if (gameStage <= MIDDLE_GAME_TRESHOLD) {
			t = (double) (gameStage - GameStage.FIRST) / (double) (MIDDLE_GAME_TRESHOLD - GameStage.FIRST);
			shift = ENDING_SHIFT;
		}
		else {
			t = (double) (gameStage - MIDDLE_GAME_TRESHOLD) / (double) (GameStage.LAST - 1 - MIDDLE_GAME_TRESHOLD);
			shift = MIDDLE_GAME_SHIFT;
		}

		final int alpha = Utils.roundToInt(MAX_ALPHA * t);

		return new CombinedEvaluationDecoder(
			shift,
			alpha
		);
	}

	public static CombinedEvaluationDecoder getDecoderForGameStage (final int gameStage) {
		return COMBINED_EVALUATION_DECODERS[gameStage];
	}

	public static long combine (final int evaluationOpening, final int evaluationMiddle, final int evaluationEnding) {
		return ((long) evaluationOpening << OPENING_SHIFT) +
		       ((long) evaluationMiddle << MIDDLE_GAME_SHIFT) +
				((long) evaluationEnding << ENDING_SHIFT);
	}

	public static int getComponentMultiplicator (final int gameStage, final int component) {
		return (int) (getDecoderForGameStage(gameStage).getMultiplicator() >>> (component * COMPONENT_SHIFT)) & EVALUATION_MASK;
	}
}
