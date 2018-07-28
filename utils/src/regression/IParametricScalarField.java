package regression;

import collections.ImmutableEnumSet;
import math.IMatrixRead;
import math.IVectorRead;

import java.util.EnumSet;
import java.util.Set;

public interface IParametricScalarField<P> {

    /**
     * Returns dimension of the input vectors.
     * @return input dimension
     */
    public int getInputDimension();

    /**
     * Returns required characteristics at given point.
     */
    public ScalarPointCharacteristics calculate(final IVectorRead x, final P parameter, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics);

    /**
     * Returns value at given point.
     */
    public default double calculateValue(final IVectorRead x, final P parameter) {
        return calculate(x, parameter, ScalarFieldCharacteristic.SET_VALUE).getValue();
    }

    /**
     * Returns gradient at given point.
     */
    public default IVectorRead calculateGradient(final IVectorRead x, final P parameter) {
        return calculate(x, parameter, ScalarFieldCharacteristic.SET_GRADIENT).getGradient();
    }

    /**
     * Returns gradient at given point.
     */
    public default IMatrixRead calculateHessian(final IVectorRead x, final P parameter) {
        return calculate(x, parameter, ScalarFieldCharacteristic.SET_HESSIAN).getHessian();
    }

}
