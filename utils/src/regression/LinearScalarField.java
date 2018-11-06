package regression;

import collections.ImmutableEnumSet;
import math.*;

public class LinearScalarField implements IScalarField {

    private final IVectorRead coeffs;
    private final IMatrixRead hessian;

    public LinearScalarField(final IVectorRead coeffs) {
        this.coeffs = coeffs.immutableCopy();

        final int dimension = coeffs.getDimension();
        this.hessian = Matrices.getZeroMatrix(dimension, dimension);
    }

    @Override
    public int getInputDimension() {
        return coeffs.getDimension();
    }

    @Override
    public ScalarPointCharacteristics calculate(final IVectorRead x, final Void parameter, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        return new ScalarPointCharacteristics(
                () -> coeffs.dotProduct(x),
                () -> coeffs,
                () -> hessian,
                characteristics
        );
    }
}
