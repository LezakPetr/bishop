package math;

import regression.LogisticRegressionCostField;

public class OnlineLogisticModel implements IErrorAccumulator {
	private final IErrorAccumulator errorAccumulator;
	private double gamma = 1e-4;   // Speed of learning
	private double lambda = 0.0;   // Regularization
	private double intercept;
	private double slope;

	public OnlineLogisticModel(final IErrorAccumulator errorAccumulator) {
		this.errorAccumulator = errorAccumulator;

		clear();
	}

	public OnlineLogisticModel() {
		this(NullErrorAccumulator.getInstance());
	}

	@Override
	public void addSample (final double x, final int y) {
		final double probability = getProbability(x);
		final double probDiff = probability - y;
		intercept -= gamma * (probDiff + 2 * lambda * intercept);
		slope -= gamma * (probDiff * x + 2 * lambda * intercept);

		errorAccumulator.addSample(probability, y);
	}

	public double getProbability (final double x) {
		final double z = getExcitation(x);

		return LogisticRegressionCostField.sigmoid(z);
	}

	public double getExcitation(final double x) {
		return intercept + x * slope;
	}

	@Override
	public void clear() {
		intercept = 0.5;
		slope = 1.0;
		errorAccumulator.clear();
	}

}
