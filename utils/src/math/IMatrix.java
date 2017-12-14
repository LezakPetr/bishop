package math;

public interface IMatrix extends IMatrixRead {
	/**
	 * Sets element on given row and column.
	 * @param row row index
	 * @param column column index
	 * @value element on given row and column
	 */
	public IMatrix setElement (final int row, final int column, final double value);
	
	public IMatrixRead freeze();

	@Override
	public IVector getRowVector(final int index);

	public default void setRowVector(final int index, final IVectorRead row) {
		getRowVector(index).assign(row);
	}

}
