package regression;

import collections.ImmutableEnumSet;
import collections.ImmutableList;
import math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScalarFieldSum<P> implements IParametricScalarField<P> {

    private final int inputDimension;
    private final List<IParametricScalarField<? super P>> operandList;

    @SafeVarargs
    public ScalarFieldSum(final int inputDimension, final IParametricScalarField<? super P> ...operands) {
        this (inputDimension, Arrays.asList(operands));
    }

    public ScalarFieldSum(final int inputDimension, final List<IParametricScalarField<? super P>> operands) {
        this.inputDimension = inputDimension;
        this.operandList = ImmutableList.copyOf(operands);

        for (IParametricScalarField<?> operand: operandList) {
            if (operand.getInputDimension() != inputDimension)
                throw new RuntimeException("Different input dimensions");
        }
    }

    @Override
    public int getInputDimension() {
        return inputDimension;
    }

    @Override
    public ScalarPointCharacteristics calculate(final IVectorRead x, final P parameter, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
        double value = (characteristics.contains(ScalarFieldCharacteristic.VALUE)) ? 0.0 : Double.NaN;
        IVector gradient = (characteristics.contains(ScalarFieldCharacteristic.GRADIENT)) ? Vectors.vectorWithDensity(Density.SPARSE, inputDimension) : null;
        IMatrix hessian = (characteristics.contains(ScalarFieldCharacteristic.HESSIAN)) ? Matrices.createMutableMatrix(Density.SPARSE, inputDimension, inputDimension) : null;

        for (IParametricScalarField<? super P> operand: operandList) {
            final ScalarPointCharacteristics scalarPointCharacteristics = operand.calculate(x, parameter, characteristics);

            if (characteristics.contains(ScalarFieldCharacteristic.VALUE))
                value += scalarPointCharacteristics.getValue();

            if (characteristics.contains(ScalarFieldCharacteristic.GRADIENT))
                Vectors.addInPlace(gradient, scalarPointCharacteristics.getGradient());

            if (characteristics.contains(ScalarFieldCharacteristic.HESSIAN))
                Matrices.addInPlace(hessian, scalarPointCharacteristics.getHessian());
        }

        return new ScalarPointCharacteristics(value, (gradient != null) ? gradient.freeze() : null, (hessian != null) ? hessian.freeze() : null);
    }

}
