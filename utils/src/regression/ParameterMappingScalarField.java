package regression;

import math.IVectorRead;
import math.Vectors;

import java.util.function.Function;

public class ParameterMappingScalarField<P, S> implements IParametricScalarField<P> {
    private final IParametricScalarField<S> baseField;
    private final Function<? super P, ? extends S> parameterMapper;

    public ParameterMappingScalarField(final IParametricScalarField<S> baseField, final Function<? super P, ? extends S> parameterMapper) {
        this.baseField = baseField;
        this.parameterMapper = parameterMapper;
    }

    @Override
    public int getInputDimension() {
        return baseField.getInputDimension();
    }

    @Override
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final P parameter) {
        return baseField.calculateValueAndGradient(x, parameterMapper.apply(parameter));
    }

    @Override
    public double calculateValue(final IVectorRead x, final P parameter) {
        return baseField.calculateValue(x, parameterMapper.apply(parameter));
    }

    @Override
    public IVectorRead calculateGradient(final IVectorRead x, final P parameter) {
        return baseField.calculateGradient(x, parameterMapper.apply(parameter));
    }

}
