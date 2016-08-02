package math;

import java.util.Arrays;

public class VectorImpl implements IVector {
	
	private final double[] elements;
	
	public VectorImpl (final double[] elements) {
		this.elements = Arrays.copyOf(elements, elements.length);
	}
	
	/**
	 * Returns dimension of this vector.
	 * @return number of elements of this vector
	 */
	public int getDimension() {
		return elements.length;
	}

	/**
	 * Returns element with given index.
	 * @param index index of element
	 * @return value of element
	 */
	public double getElement (final int index) {
		return elements[index];
	}

}
