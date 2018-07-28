package regression;


import math.*;

import java.util.Random;

public class NewtonSolver {

    private final IScalarField costField;
    private long maxIterations = 100;
    private IVectorRead input;
    private double epsilon = 1e-9;

    public NewtonSolver (final int equationCount, final IScalarField costField) {
        this.costField = costField;
        this.input = Vectors.getRandomVector(new Random(), equationCount);
    }

    public IVectorRead solve() {
        ScalarPointCharacteristics previousPoint = costField.calculate(input, null, ScalarFieldCharacteristic.SET_ALL);
        int omegaExponent = 0;

        for (int i = 0; i < maxIterations; i++) {
            final IMatrixRead invJ = Matrices.inverse(previousPoint.getHessian());
            final double omega = Math.pow(2, omegaExponent);

            final IVectorRead nextInput = Vectors.minus(
                    input,
                    Vectors.multiply(omega, Matrices.multiply(invJ, previousPoint.getGradient()))
            );

            final ScalarPointCharacteristics nextPoint = costField.calculate(nextInput, null, ScalarFieldCharacteristic.SET_ALL);

            System.out.println ("Iteration = " + i + ", omega = " + omega + ", residuum = " + nextPoint.getValue());
            final double costDiff = previousPoint.getValue() - nextPoint.getValue();

            if (costDiff >= 0) {
                input = nextInput;
                previousPoint = nextPoint;
                omegaExponent = Math.min(omegaExponent + 1, 0);

                if (costDiff / omega <= epsilon)
                    return input;
            }
            else
                omegaExponent--;
        }

        return null;
    }

    public void setMaxIterations(final long count) {
        this.maxIterations = count;
    }

}
