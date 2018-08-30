package regression;

import collections.ImmutableEnumSet;
import math.*;

import java.util.BitSet;
import java.util.Collection;

public class Regularization implements IParametricScalarField<Long> {
    private final int inputDimension;
    private final BitSet featureSet;
    private double lambda = 1.0;

    public Regularization (final int inputDimension) {
        this.inputDimension = inputDimension;
        this.featureSet = new BitSet(inputDimension);
    }

    public void addFeatures(final Collection<Integer> features) {
        for (int feature: features)
            featureSet.set(feature);
    }

    @Override
    public int getInputDimension() {
        return inputDimension;
    }

    @Override
    public ScalarPointCharacteristics calculate(final IVectorRead x, final Long sampleCount, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        final double coeff = lambda / sampleCount;

        double value = 0.0;
        final IVector gradient = Vectors.dense(inputDimension);
        final IMatrix hessian = Matrices.createMutableMatrix(Density.SPARSE, inputDimension, inputDimension);

        for (int i = 0; i < inputDimension; i++) {
            if (featureSet.get(i)) {
                final double xi = x.getElement(i);
                value += coeff * Utils.sqr(xi);
                gradient.setElement(i, 2 * coeff * xi);
                hessian.setElement(i, i, 2 * coeff);
            }
        }

        final double finalValue = value;

        return new ScalarPointCharacteristics(
                () -> finalValue,
                () -> gradient.freeze(),
                () -> hessian.freeze(),
                characteristics
        );
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(final double lambda) {
        this.lambda = lambda;
    }
}
