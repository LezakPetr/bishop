package regression;

import math.IVectorRead;
import math.Vectors;

import java.util.function.DoubleUnaryOperator;

public class ValueMappingScalarField<P> implements IParametricScalarField<P> {
    private final IParametricScalarField<P> baseField;
    private final DoubleUnaryOperator function;
    private final DoubleUnaryOperator derivation;

    public ValueMappingScalarField(final IParametricScalarField<P> baseField, final DoubleUnaryOperator function, final DoubleUnaryOperator derivation) {
        this.baseField = baseField;
        this.function = function;
        this.derivation = derivation;
    }

    @Override
    public int getInputDimension() {
        return baseField.getInputDimension();
    }

    @Override
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final P parameter) {
        final ScalarWithGradient baseValueAndGradient = baseField.calculateValueAndGradient(x, parameter);

        return new ScalarWithGradient(
                function.applyAsDouble(baseValueAndGradient.getScalar()),
                Vectors.multiply(
                        derivation.applyAsDouble(baseValueAndGradient.getScalar()),
                        baseValueAndGradient.getGradient()
                )
        );
    }

    @Override
    public double calculateValue(final IVectorRead x, final P parameter) {
        return function.applyAsDouble(baseField.calculateValue(x, parameter));
    }

    @Override
    public IVectorRead calculateGradient(final IVectorRead x, final P parameter) {
        final ScalarWithGradient baseValueAndGradient = baseField.calculateValueAndGradient(x, parameter);

        return Vectors.multiply(
                derivation.applyAsDouble(baseValueAndGradient.getScalar()),
                baseValueAndGradient.getGradient()
        );
    }

}
