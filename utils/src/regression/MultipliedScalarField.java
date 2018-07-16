package regression;

import math.IVectorRead;
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
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final P parameter) {
        final ScalarWithGradient scalarWithGradient = baseField.calculateValueAndGradient(x, parameter);

        return new ScalarWithGradient(
                coeff * scalarWithGradient.getScalar(),
                Vectors.multiply(coeff, scalarWithGradient.getGradient())
        );
    }

    @Override
    public double calculateValue(final IVectorRead x, final P parameter) {
        return coeff * baseField.calculateValue(x, parameter);
    }

    @Override
    public IVectorRead calculateGradient(final IVectorRead x, final P parameter) {
        return Vectors.multiply(coeff, baseField.calculateGradient(x, parameter));
    }

    public static <P> IParametricScalarField<P> of (final double coeff, final IParametricScalarField<P> baseField) {
        if (coeff == 1)
            return baseField;
        else
            return new MultipliedScalarField<>(coeff, baseField);
    }

}
