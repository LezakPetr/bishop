package regression;

import collections.ImmutableEnumSet;
import math.IVectorRead;
import math.Matrices;
import math.Vectors;

public class MultipliedScalarField<P> implements IParametricScalarField<P> {
    private final IParametricScalarField<P> baseField;
    private final double coeff;

    private MultipliedScalarField(final double coeff, final IParametricScalarField<P> baseField) {
        this.baseField = baseField;
        this.coeff = coeff;
    }

    @Override
    public int getInputDimension() {
        return baseField.getInputDimension();
    }

    @Override
    public ScalarPointCharacteristics calculate(final IVectorRead x, final P parameter, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        final ScalarPointCharacteristics basePointCharacteristics = baseField.calculate(x, parameter, characteristics);

        return new ScalarPointCharacteristics(
                () -> coeff * basePointCharacteristics.getValue(),
                () -> basePointCharacteristics.getGradient().multiply(coeff),
                () -> Matrices.multiply(coeff, basePointCharacteristics.getHessian()),
                characteristics
        );
    }

    public static <P> IParametricScalarField<P> of (final double coeff, final IParametricScalarField<P> baseField) {
        if (coeff == 1)
            return baseField;
        else
            return new MultipliedScalarField<>(coeff, baseField);
    }

}
