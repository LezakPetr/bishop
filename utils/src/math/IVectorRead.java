package math;

import java.util.function.DoubleUnaryOperator;

public interface IVectorRead extends Comparable<IVectorRead> {
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
	
	
	public IVectorRead subVector(final int begin, final int end);
	
	public Density density();
	
	public IVectorIterator getNonZeroElementIterator();

	public default int getNonZeroElementCount() {
		int count = 0;

		for (IVectorIterator it = getNonZeroElementIterator(); it.isValid(); it.next())
			count++;

		return count;
	}

	/**
	 * Returns true if it is guaranteed that the vector will not change value.
	 * @return if vector is immutable
	 */
	public boolean isImmutable();

	/**
	 * Returns mutable copy of given vector.
	 */
	public default IVector copy() {
		return UnaryVectorAlgorithm.getInstance().processElements(this, DoubleUnaryOperator.identity(), new VectorSetter()).getMutableVector();
	}

	/**
	 * Returns immutable copy of given vector.
	 */
	public default IVectorRead immutableCopy() {
		if (isImmutable())
			return this;
		else
			return copy().freeze();
	}

	/**
	 * Checks if vector is zero.
	 * @return true if vector is zero, false if it contains at least one nonzero element
	 */
	public default boolean isZero() {
		for (IVectorIterator it = getNonZeroElementIterator(); it.isValid(); it.next()) {
			if (it.getElement() != 0)
				return false;
		}

		return true;
	}

	/**
	 * Returns opposite vector.
	 * @return -this
	 */
	public IVectorRead negate();

	/**
	 * Adds two vectors.
	 *
	 * @param that vector
	 * @return this + that
	 */
	public IVectorRead plus(final IVectorRead that);

	/**
	 * Subtracts two vectors.
	 * @param that vector
	 * @return this - that
	 */
	public IVectorRead minus (final IVectorRead that);

	/**
	 * Multiplies vector by scalar.
	 * @param c scalar
	 * @return c * this
	 */
	public IVectorRead multiply (final double c);

	/**
	 * Multiplies two vectors element by element.
	 * @param that vector
	 * @return vector with elements equal to products of corresponding elements of input vectors
	 */
	public IVectorRead elementMultiply (final IVectorRead that);

	/**
	 * Divides two vectors element by element.
	 * @param that vector
	 * @return this / that
	 */
	public IVectorRead elementDivide (final IVectorRead that);

	/**
	 * Makes the vector unit. Result is undefined for zero vector.
	 * @return unit vector with same direction
	 */
	public IVectorRead normalize();

	/**
	 * Returns length of the vector.
	 * @return length
	 */
	public double getLength();

	/**
	 * Calculates this + that*coeff
	 */
	public IVectorRead multiplyAndAdd(final IVectorRead that, final double coeff);

	/**
	 * Calculates dot product of two vectors.
	 */
	public double dotProduct(final IVectorRead that);

	/**
	 * Multiplies given vector by given matrix.
	 * @param m input matrix
	 * @return vector v*m
	 */
	public IVectorRead multiply (final IMatrixRead m);

}
