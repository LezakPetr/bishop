package regression;

import math.IVectorRead;

/**
 * Sample is a sample function value in the regression.
 * It holds several inputs, several outputs and weight.
 */
public interface ISample {
	/**
	 * Returns vector of input values.
	 * @return input vector
	 */
	public IVectorRead getInput();

	/**
	 * Returns vector of output values.
	 * @return output vector
	 */
	public IVectorRead getOutput();

	/**
	 * Returns weight of the sample.
	 * @return weight
	 */
	public double getWeight();
}
