package math;

import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

public interface IVector {
	/**
	 * Returns dimension of this vector.
	 * @return number of elements of this vector
	 */
	public int getDimension(); 

	/**
	 * Returns element with given index.
	 * @param index index of element
	 * @return value of element
	 */
	public double getElement (final int index);
}
