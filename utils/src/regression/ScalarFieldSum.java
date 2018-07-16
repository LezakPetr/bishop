package regression;

import math.IVectorRead;
import math.Vectors;

import java.util.ArrayList;
import java.util.List;

public class ScalarFieldSum<P> implements IParametricScalarField<P> {

    private final int inputDimension;
    private final List<IParametricScalarField<? super P>> operandList = new ArrayList<>();

    public ScalarFieldSum(final int inputDimension) {
        this.inputDimension = inputDimension;
    }

    @Override
    public int getInputDimension() {
        return inputDimension;
    }

    @Override
    public ScalarWithGradient calculateValueAndGradient(final IVectorRead x, final P parameter) {
        double value = 0.0;
        IVectorRead gradient = Vectors.getZeroVector(inputDimension);

        for (IParametricScalarField<? super P> operand: operandList) {
            final ScalarWithGradient scalarWithGradient = operand.calculateValueAndGradient(x, parameter);
            value += scalarWithGradient.getScalar();
            gradient = Vectors.plus(gradient, scalarWithGradient.getGradient());
        }

        return new ScalarWithGradient(value, gradient);
    }

    @Override
    public double calculateValue(final IVectorRead x, final P parameter) {
        double value = 0.0;

        for (IParametricScalarField<? super P> operand: operandList)
            value += operand.calculateValue(x, parameter);

        return value;
    }

    @Override
    public IVectorRead calculateGradient(final IVectorRead x, final P parameter) {
        IVectorRead gradient = Vectors.getZeroVector(inputDimension);

        for (IParametricScalarField<? super P> operand: operandList)
            gradient = Vectors.plus(gradient, operand.calculateGradient(x, parameter));

        return gradient;
    }
}
