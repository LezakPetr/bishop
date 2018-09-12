package regression;


import math.*;

import java.util.Random;

/**
 * Newton solver finds minimum of given cost field by solving set of (nonlinear) equation
 * that the partial derivations of the cost field against input is zero.
 */
public class NewtonSolver {

	private static final double OMEGA_BASE = Math.sqrt(2);
	private static final int MIN_OMEGA_EXPONENT = -20;

    private final IParametricScalarField<Void> costField;
    private long maxIterations = 100;
    private IVectorRead input;
    private double omega;
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

        for (int i = 0; i < maxIterations; i++) {
        	final IVectorRead dInput = new CholeskySolver(previousPoint.getHessian(), previousPoint.getGradient()).solve();
			selectNextInput(dInput);

			final ScalarPointCharacteristics nextPoint = costField.calculate(input, null, ScalarFieldCharacteristic.SET_ALL);

			final double nextValue = nextPoint.getValue();
            final double costDiff = previousPoint.getValue() - nextValue;
            System.out.println ("Iteration = " + i + ", cost = " + nextPoint.getValue() + ", costDiff = " + costDiff);

			if (costDiff / omega <= epsilon)
				return input;

			previousPoint = nextPoint;
        }

        return null;
    }

	/**
	 * Select next input based on the current input and input diff.
	 * It starts with the full dInput and then it shortens it until the value is decreasing.
	 * It then returns the next input with minimal value. It is guaranteed that the value
	 * decreases compared to previous iteration if minimal omega is not reached.
	 * The method also sets omega.
	 * @param dInput input difference that should move input to minimum if the equations are linear
	 */
	private void selectNextInput (final IVectorRead dInput) {
    	double previousValue = Double.POSITIVE_INFINITY;
    	IVectorRead previousInput = null;

    	for (int omegaExponent = 0; omegaExponent >= MIN_OMEGA_EXPONENT; omegaExponent--) {
			final double nextOmega = Math.pow(OMEGA_BASE, omegaExponent);

			final IVectorRead nextInput = Vectors.minus(
					input,
					Vectors.multiply(nextOmega, dInput)
			);

			final double nextValue = costField.calculateValue(nextInput, null);
			System.out.println ("Omega = " + nextOmega + ", cost = " + nextValue);

			if (nextValue >= previousValue)
				break;

			previousValue = nextValue;
			previousInput = nextInput;
			omega = nextOmega;
		}

		input = previousInput;
	}

    public void setMaxIterations(final long count) {
        this.maxIterations = count;
    }

    public void setEpsilon (final double epsilon) {
    	this.epsilon = epsilon;
	}

}
