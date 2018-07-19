package regression;

import math.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LogisticRegression {

    private static final int INITIAL_BATCH_SIZE = 256;
    private static final int BATCH_SIZE_MULTIPLIER = 8;
    private static final double MAX_TERMINATION_GRADIENT_LENGTH = 1e-9;

    private final int inputDimension;
    private final int outputIndex;
    private final LinearFeatureCombination featureCombination;
    private IVector regularization;
    private double lambda = 1;

    private NewtonSolver solver;
    private final List<ISample> sampleList = new ArrayList<>();

    private class OutputAndJacobian {
        private final int featureCount;
        private final IMatrix jacobian;
        private final IVector output;

        public OutputAndJacobian (final int featureCount) {
            this.featureCount = featureCount;
            this.jacobian = Matrices.createMutableMatrix(Density.SPARSE, featureCount, featureCount);
            this.output = Vectors.dense(featureCount);
        }

        public OutputAndJacobian add (final OutputAndJacobian that) {
            for (int rowIndex = 0; rowIndex < featureCount; rowIndex++) {
                for (IVectorIterator colIt = that.jacobian.getRowVector(rowIndex).getNonZeroElementIterator(); colIt.isValid(); colIt.next()) {
                    final int columnIndex = colIt.getIndex();

                    this.jacobian.addElement(rowIndex, columnIndex, colIt.getElement());
                }
            }

            for (int rowIndex = 0; rowIndex < featureCount; rowIndex++) {
                this.output.addElement (rowIndex, that.output.getElement(rowIndex));
            }

            return this;
        }

        public OutputAndJacobian addSample (final ISample sample, final IVectorRead theta) {
            final ScalarWithGradient features = featureCombination.calculateValueAndGradient(theta, sample);
            final IVectorRead gradient = features.getGradient();
            final double p = LogisticRegressionCostField.sigmoid(features.getScalar());
            final double pDerivation = (1 - p) * p;

            for (IVectorIterator itA = gradient.getNonZeroElementIterator(); itA.isValid(); itA.next()) {
                for (IVectorIterator itB = gradient.getNonZeroElementIterator(); itB.isValid(); itB.next()) {
                    jacobian.addElement(itA.getIndex(), itB.getIndex(), itA.getElement() * itB.getElement() * pDerivation);
                }

                output.addElement(itA.getIndex(), (p - sample.getOutput().getElement(outputIndex)) * itA.getElement());
            }

            return this;
        }
    }

    public LogisticRegression(final int inputDimension, final int outputIndex) {
        this.inputDimension = inputDimension;
        this.outputIndex = outputIndex;

        this.featureCombination = new LinearFeatureCombination();
    }

    private NonLinearEquationSystemPoint equation (final IVectorRead theta) {
        final int featureCount = featureCombination.getInputDimension();

        final OutputAndJacobian samplesSummary = sampleList.parallelStream().collect(
                () -> new OutputAndJacobian (featureCount),
                (state, sample) -> state.addSample(sample, theta),
                OutputAndJacobian::add
        );

        final double m = sampleList.size();
        final IMatrix regularizationTerm = Matrices.createMutableMatrix(Density.SPARSE, featureCount, featureCount);

        for (int i = 0; i < featureCount; i++)
            regularizationTerm.setElement(i, i, 2 * lambda * regularization.getElement(i));

        return new NonLinearEquationSystemPoint(
                theta,
                Vectors.plus(Vectors.multiply(1.0 / m, samplesSummary.output), Vectors.multiply(2 * lambda, Vectors.elementMultiply(regularization, theta))),
                Matrices.plus(Matrices.multiply(1.0 / m, samplesSummary.jacobian), regularizationTerm)
        );
    }

    public void addFeature(final IScalarField feature) {
        featureCombination.addFeature(feature);
    }

    public void addLinearFeature (final int inputIndex) {
        addFeature(PolynomialScalarField.getUnitField(inputIndex, inputDimension));
    }

    public void addRegularization(final int inputIndex) {
        regularization.setElement(inputIndex, 1.0);
    }

    public void addSamples (final Collection<? extends ISample> sampleList) {
        this.sampleList.addAll(sampleList);
    }

    public void initialize() {
        this.solver = new NewtonSolver(featureCombination.getInputDimension(), this::equation);
        this.regularization = Vectors.dense(featureCombination.getInputDimension());
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
