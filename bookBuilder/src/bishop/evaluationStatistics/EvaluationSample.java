package bishop.evaluationStatistics;

import bishop.engine.FeatureCountPositionEvaluation;
import math.Utils;
import neural.ISample;

public class EvaluationSample implements ISample {

	private final short[] inputs;   // Contains pairs [index, value]
	private final float output;
	
	public EvaluationSample (final FeatureCountPositionEvaluation evaluation, final double inputCoeff, final float output) {
		this.output = output;
		
		final int nonZeroCoeffCount = evaluation.genNonZeroFeatureCount();
		this.inputs = new short[2 * nonZeroCoeffCount];
		
		for (int i = 0; i < nonZeroCoeffCount; i++) {
			final int coeff = evaluation.getNonZeroFeatureAt(i);
			
			if (coeff >= Short.MAX_VALUE)
				throw new RuntimeException("Coeff overflow");
			
			inputs[2 * i] = (short) coeff;
			inputs[2 * i + 1] = Utils.roundToShort(evaluation.getFeatureCount(coeff) * inputCoeff);
		}
	}
	
	@Override
	public int getInputIndex(final int nonZeroIndex) {
		return inputs[2 * nonZeroIndex];
	}

	@Override
	public float getInputValue(final int nonZeroIndex) {
		return inputs[2 * nonZeroIndex + 1];
	}

	@Override
	public int getNonZeroInputCount() {
		return inputs.length >> 1;
	}

	@Override
	public float getOutput(final int index) {
		return output;
	}

	@Override
	public float getWeight() {
		return 1;
	}

}
