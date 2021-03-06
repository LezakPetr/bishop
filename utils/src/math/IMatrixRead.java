package math;

public interface IMatrixRead {

	/**
	 * Returns number of rows.
	 *
	 * @return number of rows
	 */
	public int getRowCount();

	/**
	 * Returns number of columns.
	 *
	 * @return number of columns
	 */
	public int getColumnCount();

	/**
	 * Returns element on given row and column.
	 *
	 * @param row    row index
	 * @param column column index
	 * @return element on given row and column
	 */
	public double getElement(final int row, final int column);

	/**
	 * Creates a mutable copy of this matrix.
	 *
	 * @return copy
	 */
	public IMatrix copy();

	/**
	 * Returns immutable copy of given matrix.
	 * Returns the matrix itself if it is already immutable.
	 */
	public default IMatrixRead immutableCopy() {
		if (isImmutable())
			return this;
		else
			return copy().freeze();
	}

	/**
	 * Returns row vector with given index.
	 *
	 * @param index row index
	 * @return row vector
	 */
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
	 *
	 * @return if matrix is immutable
	 */
	public boolean isImmutable();


	public IMatrixRowIterator getNonZeroRowIterator();

	/**
	 * Checks if matrix is zero.
	 *
	 * @return true if matrix is zero, false if it contains at least one nonzero element
	 */
	public boolean isZero();

	public IMatrixRead transpose();

	/**
	 * Adds two matrices.
	 * @param that matrix
	 * @return this + that
	 */
	public IMatrixRead plus (final IMatrixRead that);

	/**
	 * Subtracts two matrices.
	 * @param that matrix
	 * @return this - that
	 */
	public IMatrixRead minus (final IMatrixRead that);

	/**
	 * Multiplies two matrices.
	 * @param that input matrix
	 * @return matrix this * that
	 */
	public IMatrixRead multiply (final IMatrixRead that);

	/**
	 * Multiplies matrix with constant.
	 * @param c constant
	 * @return matrix c * this
	 */
	public IMatrixRead multiply (final double c);

	/**
	 * Multiplies given matrix by given vector.
	 * @param v input vector
	 * @return vector that * v
	 */
	public IVectorRead multiply (final IVectorRead v);

	/**
	 * Returns maximal absolute element in the matrix.
	 * @return maximal absolute element
	 */
	public double maxAbsElement();

	/**
	 * Returns opposite matrix.
	 * @return -this
	 */
	public IMatrixRead negate();

}
