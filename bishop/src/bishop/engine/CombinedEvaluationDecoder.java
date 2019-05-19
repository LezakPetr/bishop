package bishop.engine;

public class CombinedEvaluationDecoder {
	private final int shift;
	private final int alpha;

	public CombinedEvaluationDecoder(final int shift, final int alpha) {
		this.shift = shift;
		this.alpha = alpha;
	}

	public int decode (final long combinedEvaluation) {
		final long shiftedEvaluation = combinedEvaluation >>> shift;

		final int component1 = ((int) shiftedEvaluation & CombinedEvaluation.EVALUATION_MASK) * (CombinedEvaluation.MAX_ALPHA - alpha);
		final int component2 = ((int) (shiftedEvaluation >>> CombinedEvaluation.COMPONENT_SHIFT) & CombinedEvaluation.EVALUATION_MASK) * alpha;

		return (component1 + component2 - CombinedEvaluation.EVALUATION_BASE * CombinedEvaluation.MAX_ALPHA) >> CombinedEvaluation.ALPHA_BITS;
	}

	public long getMultiplicator() {
		return ((long) (CombinedEvaluation.MAX_ALPHA - alpha) +
		       ((long) alpha << CombinedEvaluation.COMPONENT_SHIFT)) << shift;
	}
}
