package regression;


import math.*;

import java.util.Random;

/**
 * Newton solver finds minimum of given cost field by solving set of (nonlinear) equation
 * that the partial derivations of the cost field against input is zero.
 */
public class NewtonSolver {

	private static final int OMEGA_ITERATION_COUNT = 16;
	private static final double MAX_OMEGA = 16.0;

    private final IParametricScalarField<Void> costField;
    private long maxIterations = 200;
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

			if (costDiff >= -epsilon && costDiff / omega <= epsilon)
				return input;

			previousPoint = nextPoint;
        }

        return null;
    }

	/**
	 * Select next input based on the current input and input diff.
	 * The method also sets omega.
	 * @param dInput input difference that should move input to minimum if the equations are linear
	 */
	private void selectNextInput (final IVectorRead dInput) {
    	double minOmega = MAX_OMEGA;
    	double dOmega = MAX_OMEGA / 2;

		IVectorRead minInput = getInputForOmega(dInput, minOmega);
		double minValue = costField.calculateValue(minInput, null);

    	for (int omegaIteration = 0; omegaIteration < OMEGA_ITERATION_COUNT; omegaIteration++) {
    		double prevOmega = minOmega - dOmega;
			double nextOmega = minOmega + dOmega;

			final IVectorRead nextInput = getInputForOmega(dInput, nextOmega);
			final double nextValue = costField.calculateValue(nextInput, null);

			final IVectorRead prevInput = getInputForOmega(dInput, prevOmega);
			final double prevValue = costField.calculateValue(prevInput, null);

			if (prevValue < minValue) {
				minValue = prevValue;
				minOmega = prevOmega;
				minInput = prevInput;
			}

			if (nextValue < minValue) {
				minValue = nextValue;
				minOmega = nextOmega;
				minInput = nextInput;
			}

			System.out.println ("Omega = " + minOmega + ", cost = " + minValue);

			dOmega /= 2;
		}

		input = minInput;
    	omega = minOmega;
	}

	private IVectorRead getInputForOmega(IVectorRead dInput, double omega) {
		return input.minus(dInput.multiply(omega));
	}

	public void setMaxIterations(final long count) {
        this.maxIterations = count;
    }

    public void setEpsilon (final double epsilon) {
    	this.epsilon = epsilon;
	}

}
