package math;

public interface IMatrixRead {
	
	/**
	 * Returns number of rows.
	 * @return number of rows
	 */
	public int getRowCount();
	
	/**
	 * Returns number of columns.
	 * @return number of columns
	 */
	public int getColumnCount();
	
	/**
	 * Returns element on given row and column.
	 * @param row row index
	 * @param column column index
	 * @return element on given row and column
	 */
	public double getElement (final int row, final int column);

	/**
	 * Creates a mutable copy of this matrix.
	 * @return copy
	 */
	public IMatrix copy();
	
	public IVectorRead getRowVector(final int index);
	
	public default IVectorRead getColumnVector(final int column) {
		return new AbstractVectorRead() {
			@Override
			public IVectorRead subVector(final int begin, final int end) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public double getElement(final int index) {
				return IMatrixRead.this.getElement(index, column);
			}
			
			@Override
			public int getDimension() {
				return getRowCount();
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
			public boolean isImmutable() {
				return IMatrixRead.this.isImmutable();
			}
		};
	}

	public Density density();

	/**
	 * Returns true if it is guaranteed that the matrix will not change value.
	 * @return if matrix is immutable
	 */
	public boolean isImmutable();
}
