package regression;

import collections.ImmutableEnumSet;
import math.IVectorRead;
import math.Vectors;

import java.util.function.DoubleBinaryOperator;

public class SampleCostFieldImpl implements ISampleCostField {
    private final int inputDimension;
    private final IParametricScalarField<ISample> valueField;   // E.g. hypothesis
    private final DoubleBinaryOperator errorCostFunction;
    private final DoubleBinaryOperator errorCostDerivation;
    private final DoubleBinaryOperator errorCostSecondDerivation;
    private final int outputIndex;

    public SampleCostFieldImpl (final int inputDimension, final IParametricScalarField<ISample> valueField, final DoubleBinaryOperator errorCostFunction, final DoubleBinaryOperator errorCostDerivation, final DoubleBinaryOperator errorCostSecondDerivation, final int outputIndex) {
        this.inputDimension = inputDimension;
        this.valueField = valueField;
        this.errorCostFunction = errorCostFunction;
        this.errorCostDerivation = errorCostDerivation;
        this.errorCostSecondDerivation = errorCostSecondDerivation;
        this.outputIndex = outputIndex;

        if (inputDimension != valueField.getInputDimension())
            throw new RuntimeException("Mismatch between input dimensions");
    }

    @Override
    public int getInputDimension() {
        return inputDimension;
    }

    @Override
    public ScalarPointCharacteristics calculate(final IVectorRead x, final ISample sample, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        final double expectedValue = sample.getOutput().getElement(outputIndex);

        final ValueMappingScalarField<ISample> mapper = new ValueMappingScalarField<>(
                valueField,
                c -> errorCostFunction.applyAsDouble(c, expectedValue),
                c -> errorCostDerivation.applyAsDouble(c, expectedValue),
                c -> errorCostSecondDerivation.applyAsDouble(c, expectedValue)
        );

        return mapper.calculate(x, sample, characteristics);
    }
}
