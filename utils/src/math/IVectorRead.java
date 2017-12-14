package math;

public interface IVectorRead {
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
}
