package regression;


import math.*;

import java.util.Random;

public class NewtonSolver {

    private final IParametricScalarField<Void> costField;
    private long maxIterations = 100;
    private IVectorRead input;
    private double epsilon = 1e-9;

    public NewtonSolver (final int equationCount, final IParametricScalarField<Void> costField) {
        this.costField = costField;
        this.input = Vectors.getRandomVector(new Random(), equationCount);
    }

    public void setInput (final IVectorRead input) {
    	if (input.getDimension() != costField.getInputDimension())
    		throw new RuntimeException("Wrong input dimension");

    	this.input = input;
	}

    public IVectorRead solve() {
        ScalarPointCharacteristics previousPoint = costField.calculate(input, null, ScalarFieldCharacteristic.SET_ALL);
        IVectorRead dInput = null;   // Solution to the equation system. Needs to be updated only when previousPoint is changed.

        int omegaExponent = 0;

        for (int i = 0; i < maxIterations; i++) {
            if (dInput == null) {
            	System.out.println ("Inverting");
				//dInput = GaussianElimination.equationSolver(previousPoint.getHessian(), previousPoint.getGradient()).solve();
				dInput = new CholeskySolver(previousPoint.getHessian(), previousPoint.getGradient()).solve();
			}

            final double omega = Math.pow(2, omegaExponent);

            final IVectorRead nextInput = Vectors.minus(
                    input,
                    Vectors.multiply(omega, dInput)
            );

            final double nextValue = costField.calculateValue(nextInput, null);
            final double costDiff = previousPoint.getValue() - nextValue;
            System.out.println ("Iteration = " + i + ", omega = " + omega + ", residuum = " + nextValue + ", costDiff = " + costDiff);

            if (costDiff >= 0) {
                final ScalarPointCharacteristics nextPoint = costField.calculate(nextInput, null, ScalarFieldCharacteristic.SET_ALL);

                input = nextInput;
                previousPoint = nextPoint;
                omegaExponent = Math.min(omegaExponent + 1, 0);
                dInput = null;   // Previous point changed, force recalculation of dInput

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

    public void setEpsilon (final double epsilon) {
    	this.epsilon = epsilon;
	}

}
