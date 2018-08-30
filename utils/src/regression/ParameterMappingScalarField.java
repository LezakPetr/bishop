package regression;

import collections.ImmutableEnumSet;
import math.IMatrixRead;
import math.IVectorRead;

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
    public ScalarPointCharacteristics calculate(final IVectorRead x, final P parameter, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        return baseField.calculate(x, parameterMapper.apply(parameter), characteristics);
    }

    @Override
    public double calculateValue(final IVectorRead x, final P parameter) {
        return baseField.calculateValue(x, parameterMapper.apply(parameter));
    }

    @Override
    public IVectorRead calculateGradient(final IVectorRead x, final P parameter) {
        return baseField.calculateGradient(x, parameterMapper.apply(parameter));
    }

	@Override
	public IMatrixRead calculateHessian(final IVectorRead x, final P parameter) {
		return baseField.calculateHessian(x, parameterMapper.apply(parameter));
	}

}
