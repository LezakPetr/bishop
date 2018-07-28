package regression;

import math.IMatrixRead;
import math.IVectorRead;

public interface IScalarField extends IParametricScalarField<Void> {
    /**
     * Returns value at given point.
     */
    public default double calculateValue(final IVectorRead x) {
        return calculateValue(x, null);
    }

    /**
     * Returns gradient at given point.
     */
    public default IVectorRead calculateGradient(final IVectorRead x) {
        return calculateGradient(x, null);
    }

    /**
     * Returns hessian at given point.
     */
    public default IMatrixRead calculateHessian(final IVectorRead x) {
        return calculateHessian(x, null);
    }

}
