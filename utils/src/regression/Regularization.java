package regression;

import collections.ImmutableEnumSet;
import math.*;

import java.util.BitSet;
import java.util.Collection;

public class Regularization implements IParametricScalarField<Long> {
    private final int inputDimension;
    private final BitSet featureSet;
    private double lambda;

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
        double valueSum = 0.0;
        IVector gradientSum = Vectors.dense(inputDimension);

        for (int i = 0; i < inputDimension; i++) {
            if (featureSet.get(i)) {
                final double xi = x.getElement(i);
                valueSum += Utils.sqr(xi);
                gradientSum.setElement(i, xi);
            }
        }

        final double coeff = lambda / sampleCount;
        final double value = valueSum * coeff;

        return new ScalarPointCharacteristics(
                () -> value,
                () -> Vectors.multiply(2 * coeff, gradientSum),
                () -> Matrices.multiply(2, Vectors.cartesianProduct(gradientSum, gradientSum)),
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
