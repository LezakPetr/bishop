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

}
