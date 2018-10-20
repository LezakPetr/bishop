package regression;

import collections.ImmutableEnumSet;
import collections.ImmutableOrdinalMap;
import math.IVectorRead;
import math.Matrices;
import math.Vectors;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

public class ValueMappingScalarField<P> implements IParametricScalarField<P> {
    private final IParametricScalarField<P> baseField;
    private final DoubleUnaryOperator function;
    private final DoubleUnaryOperator firstDerivation;
    private final DoubleUnaryOperator secondDerivation;

    private static final ImmutableOrdinalMap<ScalarFieldCharacteristic, ImmutableEnumSet<ScalarFieldCharacteristic>> BASE_REQUIREMENTS = ImmutableOrdinalMap.<ScalarFieldCharacteristic, ImmutableEnumSet<ScalarFieldCharacteristic>>forEnum(ScalarFieldCharacteristic.class)
            .put(
                    ScalarFieldCharacteristic.VALUE,
                    ImmutableEnumSet.of(ScalarFieldCharacteristic.TOKEN, ScalarFieldCharacteristic.VALUE)
            )
            .put(
                    ScalarFieldCharacteristic.GRADIENT,
                    ImmutableEnumSet.of(ScalarFieldCharacteristic.TOKEN, ScalarFieldCharacteristic.VALUE, ScalarFieldCharacteristic.GRADIENT)
            )
            .put(
                    ScalarFieldCharacteristic.HESSIAN,
                    ImmutableEnumSet.of(ScalarFieldCharacteristic.TOKEN, ScalarFieldCharacteristic.VALUE, ScalarFieldCharacteristic.GRADIENT, ScalarFieldCharacteristic.HESSIAN)
            )
            .build();

    public ValueMappingScalarField(final IParametricScalarField<P> baseField, final DoubleUnaryOperator function, final DoubleUnaryOperator firstDerivation, final DoubleUnaryOperator secondDerivation) {
        this.baseField = baseField;
        this.function = function;
        this.firstDerivation = firstDerivation;
        this.secondDerivation = secondDerivation;
    }

    private ImmutableEnumSet<ScalarFieldCharacteristic> getBaseCharacteristics(final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        ImmutableEnumSet baseCharacteristics = ImmutableEnumSet.noneOf (ScalarFieldCharacteristic.TOKEN);

        for (ScalarFieldCharacteristic ch: characteristics) {
            baseCharacteristics = ImmutableEnumSet.union (baseCharacteristics, BASE_REQUIREMENTS.get(ch));
        }

        return baseCharacteristics;
    }

    @Override
    public int getInputDimension() {
        return baseField.getInputDimension();
    }

    @Override
    public ScalarPointCharacteristics calculate(final IVectorRead x, final P parameter, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        final ScalarPointCharacteristics basePointCharacteristics = baseField.calculate(x, parameter, getBaseCharacteristics(characteristics));

        final double firstDerivationValue = (characteristics.containsAny(ScalarFieldCharacteristic.SET_GRADIENT_HESSIAN)) ?
                firstDerivation.applyAsDouble(basePointCharacteristics.getValue()) : Double.NaN;
        final double secondDerivationValue = characteristics.contains(ScalarFieldCharacteristic.HESSIAN) ?
                secondDerivation.applyAsDouble(basePointCharacteristics.getValue()) : Double.NaN;

        final IVectorRead gradient = basePointCharacteristics.getGradient();

        return new ScalarPointCharacteristics(
                () -> function.applyAsDouble(basePointCharacteristics.getValue()),
                () -> gradient.multiply(firstDerivationValue),
                () -> Vectors.cartesianProduct(gradient, gradient).multiply(secondDerivationValue)
					.plus(basePointCharacteristics.getHessian().multiply(firstDerivationValue)),
                characteristics
        );
    }

    @Override
    public double calculateValue(final IVectorRead x, final P parameter) {
        return function.applyAsDouble(baseField.calculateValue(x, parameter));
    }

}
