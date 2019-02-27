package math;

import regression.LogisticRegressionCostField;

import java.util.Arrays;

public class OnlineLogisticModel {
	private final IErrorAccumulator errorAccumulator;
	private double gamma = 1e-4;   // Speed of learning
	private double intercept;
	private final double[] slopes;

	public OnlineLogisticModel(final int featureCount, final IErrorAccumulator errorAccumulator) {
		this.errorAccumulator = errorAccumulator;
		this.slopes = new double[featureCount];

		clear();
	}

	public OnlineLogisticModel(final int featureCount) {
		this(featureCount, NullErrorAccumulator.getInstance());
	}

	public void addSample (final int[] featureIndices, final double[] featureValues, final int y, final double weight) {
		assert featureIndices.length == slopes.length;
		assert featureValues.length == slopes.length;

		final double probability = getProbability(featureIndices, featureValues);
		final double probDiff = probability - y;
		final double update = weight * gamma * probDiff;

		intercept -= update;

		for (int i = 0; i < featureIndices.length; i++)
			slopes[i] -= update * featureValues[i];

		errorAccumulator.addSample(probability, y);
	}

	public double getProbability (final int[] featureIndices, final double[] featureValues) {
		assert featureIndices.length == slopes.length;
		assert featureValues.length == slopes.length;

		final double z = getExcitation(featureIndices, featureValues);

		return LogisticRegressionCostField.sigmoid(z);
	}

	public double getExcitation(final int[] featureIndices, final double[] featureValues) {
		assert featureIndices.length == slopes.length;
		assert featureValues.length == slopes.length;

		double excitation = intercept;

		for (int i = 0; i < featureIndices.length; i++)
			excitation += featureValues[i] * slopes[i];

		return excitation;
	}

	public void clear() {
		intercept = 0.5;
		Arrays.fill(slopes, 1.0);
		errorAccumulator.clear();
	}

	public double getIntercept() {
		return intercept;
	}

	public void setIntercept(double intercept) {
		this.intercept = intercept;
	}

	public double getSlope(final int index) {
		return slopes[index];
	}

	public void setSlope(final int index, final double slope) {
		this.slopes[index] = slope;
	}
}
