package regression;

import collections.ImmutableEnumSet;
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
    public ScalarPointCharacteristics calculate(final IVectorRead x, final Void parameter, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        return baseField.calculate(x, this.parameter, characteristics);
    }


}
