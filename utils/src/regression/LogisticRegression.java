package regression;

import math.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Logistic regression. This class takes a list of samples and returns parameters that
 * minimizes cost function.
 */
public class LogisticRegression {

    private final int inputDimension;
    private final int outputIndex;
    private final LinearFeatureCombination featureCombination;
    private final LogisticRegressionCostField costField;
    private final MultiSampleCostField multiSampleCostField;
    private final Regularization regularization;
    private final ScalarFieldSum regularizedField;
    private double lambda = 1;   // How much is regularization used

    private final NewtonSolver solver;

    public LogisticRegression(final int inputDimension, final int outputIndex, final List<IScalarField> featureList, final List<Integer> regularizedFeatures) {
        this.inputDimension = inputDimension;
        this.outputIndex = outputIndex;
        this.featureCombination = new LinearFeatureCombination();

        for (IScalarField feature: featureList)
            featureCombination.addFeature(feature);

        final int featureCount = featureList.size();
        this.costField = new LogisticRegressionCostField(featureCount, outputIndex, featureCombination);
        this.multiSampleCostField = new MultiSampleCostField(costField);

        this.regularization = new Regularization(featureCount);
        this.regularization.addFeatures(regularizedFeatures);
        this.regularizedField = new ScalarFieldSum<>(
                featureCount,
                costField,
                new ParameterMappingScalarField<ISample, Long>(
                    regularization, s -> (long) multiSampleCostField.getSampleCount()
                )
        );

        this.solver = new NewtonSolver(featureCombination.getInputDimension(), multiSampleCostField);
    }

    public void addSamples (final Collection<? extends ISample> sampleList) {
        this.multiSampleCostField.addSamples(sampleList);
    }

    public void setMaxIterations (final long count) {
        solver.setMaxIterations (count);
    }

    public IVectorRead optimize() {
        return solver.solve();
    }

    public void setLambda (final double lambda) {
        this.lambda = lambda;
    }
}
