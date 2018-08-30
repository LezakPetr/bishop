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
		final long t1 = System.currentTimeMillis();
        ScalarPointCharacteristics previousPoint = costField.calculate(input, null, ScalarFieldCharacteristic.SET_ALL);
		final long t2 = System.currentTimeMillis();
        System.out.println("residuum = " + previousPoint.getValue() + ", " + (t2 - t1) + "ms");

        IVectorRead dInput = null;   // Solution to the equation system. Needs to be updated only when previousPoint is changed.

        int omegaExponent = 0;

        for (int i = 0; i < maxIterations; i++) {
            if (dInput == null) {
            	final long t3 = System.currentTimeMillis();
            	System.out.println ("Inverting");
				//dInput = GaussianElimination.equationSolver(previousPoint.getHessian(), previousPoint.getGradient()).solve();
				dInput = new CholeskySolver(previousPoint.getHessian(), previousPoint.getGradient()).solve();
				final long t4 = System.currentTimeMillis();
				System.out.println ("Inverted " + (t4 - t3) + "ms");
			}

            final double omega = Math.pow(2, omegaExponent);

            final IVectorRead nextInput = Vectors.minus(
                    input,
                    Vectors.multiply(omega, dInput)
            );

            final ScalarPointCharacteristics nextPoint = costField.calculate(nextInput, null, ScalarFieldCharacteristic.SET_ALL);

            final double costDiff = previousPoint.getValue() - nextPoint.getValue();
            System.out.println ("Iteration = " + i + ", omega = " + omega + ", residuum = " + nextPoint.getValue() + ", costDiff = " + costDiff);

            if (costDiff >= 0) {
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
