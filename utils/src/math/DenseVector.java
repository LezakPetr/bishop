package math;

import java.util.Arrays;

public class DenseVector extends AbstractVector {
	
	private double[] elements;
	
	public DenseVector (final double[] elements) {
		this.elements = Arrays.copyOf(elements, elements.length);
	}
	
	/**
	 * Returns dimension of this vector.
	 * @return number of elements of this vector
	 */
	@Override
	public int getDimension() {
		return elements.length;
	}

	/**
	 * Returns element with given index.
	 * @param index index of element
	 * @return value of element
	 */
	@Override
	public double getElement (final int index) {
		return elements[index];
	}
	
	/**
	 * Sets element with given index.
	 * @param index index of element
	 * @value value of element
	 */
	@Override
	public DenseVector setElement (final int index, final double value) {
		checkNotFrozen();
		
		elements[index] = value;
		
		return this;
	}
	
	public void swap (final DenseVector that) {
		if (this.getDimension() != that.getDimension())
			throw new RuntimeException("Different dimensions of vectors");
		
		this.checkNotFrozen();
		that.checkNotFrozen();

		final double[] tmp = this.elements;
		
		this.elements = that.elements;
		that.elements = tmp;
	}

	@Override
	public Density density() {
		return Density.DENSE;
	}

	@Override
	public IVectorIterator getNonZeroElementIterator() {
		return new DenseNonZeroElementIterator(this);
	}

	@Override
	public IVector copy() {
		return new DenseVector(
				Arrays.copyOf(elements, elements.length)
		);
	}
}
