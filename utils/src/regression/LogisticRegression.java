package regression;

import math.*;

import java.util.*;

/**
 * Logistic regression. This class takes a list of samples and returns parameters that
 * minimizes cost function.
 */
public class LogisticRegression {

    private final int inputDimension;
    private final int outputIndex;
    private final ISampleCostField featureCombination;
    private final List<Integer> regularizedFeatures;

    private static final double BATCH_COEFF = 10.0;
	private static final double MIN_BATCH_SIZE = 1e4;

	private static final double TRAINING_SET_RATIO = 0.7;

	private final List<ISample> sampleList = new ArrayList<>();
	private long maxIterations = 500;
	private double lambda = 1.0;

	public LogisticRegression(final int inputDimension, final int outputIndex, final ISampleCostField featureCombination, final List<Integer> regularizedFeatures) {
		this.inputDimension = inputDimension;
		this.outputIndex = outputIndex;
		this.featureCombination = featureCombination;
		this.regularizedFeatures = regularizedFeatures;
	}

	private MultiSampleCostField getMultiSampleCostField (final List<ISample> sampleList) {
		final int featureCount = featureCombination.getInputDimension();
		final LogisticRegressionCostField costField = new LogisticRegressionCostField(featureCount, outputIndex, featureCombination);
		final MultiSampleCostField multiSampleCostField = new MultiSampleCostField(costField);
		multiSampleCostField.addSamples(sampleList);

		return multiSampleCostField;
	}

	private ScalarFieldSum<Void> addRegulatization (final MultiSampleCostField multiSampleCostField) {
		final int featureCount = featureCombination.getInputDimension();
		final Regularization regularization = new Regularization(featureCount);
		regularization.addFeatures(regularizedFeatures);
		regularization.setLambda(lambda);

		final ScalarFieldSum<Void> regularizedField = new ScalarFieldSum<>(
				featureCount,
				multiSampleCostField,
				new ParameterMappingScalarField<>(
						regularization, s -> (long) multiSampleCostField.getSampleCount()
				)
		);

		return regularizedField;
	}

    public LogisticRegression(final int inputDimension, final int outputIndex, final List<IScalarField> featureList, final List<Integer> regularizedFeatures) {
		this(inputDimension, outputIndex, getLinearFeatureCombination(featureList), regularizedFeatures);
    }

    private static ISampleCostField getLinearFeatureCombination(final List<IScalarField> featureList) {
		final LinearFeatureCombination featureCombination = new LinearFeatureCombination();

		for (IScalarField feature: featureList)
			featureCombination.addFeature(feature);

		return featureCombination;
	}

    public void addSamples (final Collection<? extends ISample> sampleList) {
        this.sampleList.addAll(sampleList);
    }

    public void setMaxIterations (final long count) {
        this.maxIterations = count;
    }

    public IVectorRead optimize() {
		final Random rng = new Random(56226161);
		Collections.shuffle(sampleList, rng);

		final int allSampleCount = sampleList.size();
		final int trainingSetCount = Utils.roundToInt(TRAINING_SET_RATIO * allSampleCount);
		final List<ISample> trainingSet = sampleList.subList(0, trainingSetCount);
		final List<ISample> testSet = sampleList.subList(trainingSetCount, allSampleCount);
		final int batchCount = Math.max(
				Utils.roundToInt(Math.log(trainingSetCount / MIN_BATCH_SIZE) / Math.log(BATCH_COEFF)) + 1,
				1
		);

		final int featureCount = featureCombination.getInputDimension();
		IVectorRead solution = Vectors.getZeroVector(featureCount);

		for (int i = batchCount - 1; i >= 0; i--) {
			final int batchSize = Utils.roundToInt(trainingSetCount * Math.pow(BATCH_COEFF, -i));

			final MultiSampleCostField trainingCostField = getMultiSampleCostField(trainingSet.subList(0, batchSize));
			final ScalarFieldSum<Void> regularizedField = addRegulatization(trainingCostField);
			final NewtonSolver solver = new NewtonSolver(featureCombination.getInputDimension(), regularizedField);
			solver.setInput(solution);
			solver.setMaxIterations(maxIterations);
			solution = solver.solve();

			final double trainingError = trainingCostField.calculateValue(solution);

			final MultiSampleCostField testCostField = getMultiSampleCostField(testSet);
			final double testError = testCostField.calculateValue(solution);

			System.out.println ("Batch size = " + batchSize + ", training error = " + trainingError + ", test error = " + testError);
		}

		return solution;
    }

    public void setLambda (final double lambda) {
        this.lambda = lambda;
    }
}
