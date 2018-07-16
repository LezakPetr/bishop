package regression;

import math.IVectorRead;

public class ScalarFieldWithStoredParameter<P> implements IScalarField {
    private final IParametricScalarField<? super P> baseField;
    private P parameter;

    public ScalarFieldWithStoredParameter (final IParametricScalarField<? super P> baseField) {
        this.baseField = baseField;
    }

    public ScalarFieldWithStoredParameter (final IParametricScalarField<? super P> baseField, final P parameter) {
        this.baseField = baseField;
        this.parameter = parameter;
    }

    public void setParameter (final P parameter) {
        this.parameter = parameter;
    }

    @Override
    public int getInputDimension() {
        return baseField.getInputDimension();
    }

    @Override
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final Void parameter) {
        return baseField.calculateValueAndGradient(x, this.parameter);
    }

    @Override
    public double calculateValue(final IVectorRead x, final Void parameter) {
        return baseField.calculateValue(x, this.parameter);
    }

    @Override
    public IVectorRead calculateGradient(final IVectorRead x, final Void parameter) {
        return baseField.calculateGradient(x, this.parameter);
    }

}
