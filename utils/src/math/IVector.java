package math;

public interface IVector extends IVectorRead {
	/**
	 * Sets element with given index.
	 * @param index index of element
	 * @value value of element
	 */
	public IVector setElement (final int index, final double value);

	public default IVector addElement (final int index, final double value) {
		final double oldValue = getElement(index);
		setElement(index, value + oldValue);

		return this;
	}
	
	public IVectorRead freeze();

	/**
	 * Assigns given original vector to this vector.
	 * @param orig original vector
	 */
	public default void assign (final IVectorRead orig) {
		final int dimension = this.getDimension();
		
		if (orig.getDimension() != dimension)
			throw new RuntimeException("Dimension does not match");
		
		for (int i = 0; i < dimension; i++)
			this.setElement(i, orig.getElement(i));
	}

	public default IVector subVector(final int begin, final int end) {
		return new SubVector(this, begin, end - begin);
	}
}
