package regression;

import math.IVectorRead;

public interface IParametricScalarField<P> {
    /**
     * Returns dimension of the input vectors.
     * @return input dimension
     */
    public int getInputDimension();

    /**
     * Returns value and gradient at given point.
     */
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final P parameter);

    /**
     * Returns value at given point.
     */
    public default double calculateValue(final IVectorRead x, final P parameter) {
        return calculateValueAndGradient(x, parameter).getScalar();
    }

    /**
     * Returns gradient at given point.
     */
    public default IVectorRead calculateGradient(final IVectorRead x, final P parameter) {
        return calculateValueAndGradient(x, parameter).getGradient();
    }

}
