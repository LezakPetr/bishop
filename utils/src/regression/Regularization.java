package regression;

import math.IVector;
import math.IVectorRead;
import math.Utils;
import math.Vectors;

import java.util.BitSet;

public class Regularization implements IParametricScalarField<Long> {
    private final int inputDimension;
    private final BitSet featureSet;
    private double lambda;

    public Regularization (final int inputDimension) {
        this.inputDimension = inputDimension;
        this.featureSet = new BitSet(inputDimension);
    }

    @Override
    public int getInputDimension() {
        return inputDimension;
    }

    @Override
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final Long sampleCount) {
        double valueSum = 0.0;
        IVector gradientSum = Vectors.dense(inputDimension);

        for (int i = 0; i < inputDimension; i++) {
            if (featureSet.get(i)) {
                final double xi = x.getElement(i);
                valueSum += Utils.sqr(xi);
                gradientSum.setElement(i, 2 * xi);
            }
        }

        final double coeff = lambda / sampleCount;

        return new ScalarWithGradient(
                coeff * valueSum,
                Vectors.multiply(coeff, gradientSum)
        );
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(final double lambda) {
        this.lambda = lambda;
    }
}
