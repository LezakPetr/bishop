package regression;

import math.IVectorRead;
import math.Vectors;

import java.util.function.DoubleBinaryOperator;

public class SampleCostFieldImpl implements ISampleCostField {
    private final int inputDimension;
    private final IParametricScalarField<ISample> valueField;   // E.g. hypothesis
    private final DoubleBinaryOperator errorCostFunction;
    private final DoubleBinaryOperator errorCostDerivation;
    private final int outputIndex;

    public SampleCostFieldImpl (final int inputDimension, final IParametricScalarField<ISample> valueField, final DoubleBinaryOperator errorCostFunction, final DoubleBinaryOperator errorCostDerivation, final int outputIndex) {
        this.inputDimension = inputDimension;
        this.valueField = valueField;
        this.errorCostFunction = errorCostFunction;
        this.errorCostDerivation = errorCostDerivation;
        this.outputIndex = outputIndex;

        if (inputDimension != valueField.getInputDimension())
            throw new RuntimeException("Mismatch between input dimensions");
    }

    @Override
    public int getInputDimension() {
        return inputDimension;
    }

    @Override
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final ISample sample) {
        final ScalarWithGradient valueWithGradient = valueField.calculateValueAndGradient(x, sample);

        final double value = valueWithGradient.getScalar();
        final IVectorRead gradient = valueWithGradient.getGradient();
        final double expectedValue = sample.getOutput().getElement(outputIndex);

        return new ScalarWithGradient(
                errorCostFunction.applyAsDouble(value, expectedValue),
                Vectors.multiply(errorCostDerivation.applyAsDouble(value, expectedValue), gradient)
        );
    }
}
