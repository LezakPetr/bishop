package bishop.engine;

import java.util.Arrays;

import utils.IoUtils;


public class FeatureCountPositionEvaluation extends AlgebraicPositionEvaluation {
	
	private final boolean[] featuresFilled = new boolean[PositionEvaluationFeatures.LAST];
	private final int[] featureCounts = new int[PositionEvaluationFeatures.LAST];
	private final int[] nonZeroFeatures = new int[PositionEvaluationFeatures.LAST];
	private int nonZeroFeatureCount;
	
	public FeatureCountPositionEvaluation(final PositionEvaluationFeatures features) {
		super (features);
	}

	@Override
	public void clear(final int onTurn) {
		super.clear(onTurn);
		
		Arrays.fill(featureCounts, 0);
		Arrays.fill(featuresFilled, false);
		nonZeroFeatureCount = 0;
	}
	
	@Override
	public void addCoeffWithCount(final int index, final int count) {
		if (count != 0) {
			super.addCoeffWithCount(index, count);
			
			featureCounts[index] += count;
			
			if (!featuresFilled[index]) {
				featuresFilled[index] = true;
				nonZeroFeatures[nonZeroFeatureCount] = index;
				nonZeroFeatureCount++;
			}
		}
	}
		
	public double getFeatureCount (final int index) {
		return featureCounts[index];
	}
	
	@Override
	public String toString() {
		final FeatureRegistry registry = PositionEvaluationFeatures.getFeatureRegistry();
		final StringBuilder result = new StringBuilder();
		
		for (int feature = 0; feature < PositionEvaluationFeatures.LAST; feature++) {
			final double count = getFeatureCount(feature);
			
			if (count != 0) {
				result.append (registry.getName(feature));
				result.append (" = ");
				result.append (count);
				result.append (IoUtils.NEW_LINE);
			}
		}
		
		return result.toString();
	}
	
	public int genNonZeroFeatureCount() {
		return nonZeroFeatureCount;
	}
	
	public int getNonZeroFeatureAt (final int nonZeroFeatureIndex) {
		return nonZeroFeatures[nonZeroFeatureIndex];
	}
	
}
