package regression;

import math.IVectorRead;

import java.util.Collection;

public class LogisticRegression {

    private static final int INITIAL_BATCH_SIZE = 256;
    private static final int BATCH_SIZE_MULTIPLIER = 8;
    private static final double MAX_TERMINATION_GRADIENT_LENGTH = 1e-9;

    private final int inputDimension;
    private final int outputIndex;
    private final LinearFeatureCombination featureCombination;
    private LogisticRegressionCostField costField;
    private MultiSampleCostField multiSampleCostField;
    private GradientOptimizer<Void> optimizer;

    public LogisticRegression(final int inputDimension, final int outputIndex) {
        this.inputDimension = inputDimension;
        this.outputIndex = outputIndex;

        featureCombination = new LinearFeatureCombination();
    }

    public void addFeature(final IScalarField feature) {
        featureCombination.addFeature(feature);
    }

    public void addLinearFeature (final int inputIndex) {
        addFeature(PolynomialScalarField.getUnitField(inputIndex, inputDimension));
    }

    public void addRegularization(final int inputIndex) {

    }

    public void addSamples (final Collection<? extends ISample> sampleList) {
        multiSampleCostField.addSamples(sampleList);
    }

    public void initialize() {
        costField = new LogisticRegressionCostField(featureCombination.getInputDimension(), outputIndex, featureCombination);
        multiSampleCostField = new MultiSampleCostField(costField);
        optimizer = new GradientOptimizer<>(multiSampleCostField);
    }

    public void setMaxIterations (final long count) {
        optimizer.setMaxIterations(count);
    }

    public void setAlpha (final double alpha) {
        optimizer.setAlpha (alpha);
    }

    public IVectorRead optimize() {
        optimizer.initialize();

        long batchSize = INITIAL_BATCH_SIZE;

        while (true) {
            System.out.println ("Batch size = " + batchSize);

            boolean lastRound = batchSize >= multiSampleCostField.getSampleCount();

            optimizer.setMaxTerminationGradientLength(lastRound ? MAX_TERMINATION_GRADIENT_LENGTH : 0);
            multiSampleCostField.setBatchSize((int) Math.min(batchSize, Integer.MAX_VALUE));
            optimizer.optimize(null);

            if (lastRound)
                break;

            batchSize *= BATCH_SIZE_MULTIPLIER;
        }

        System.out.println(optimizer.getOptimumOutput());

        return optimizer.getOptimumInput();
    }
}
