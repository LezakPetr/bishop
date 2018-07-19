package math;

public interface IMatrix extends IMatrixRead {
	/**
	 * Sets element on given row and column.
	 * @param row row index
	 * @param column column index
	 * @value element on given row and column
	 */
	public IMatrix setElement (final int row, final int column, final double value);

	public default IMatrix addElement (final int row, final int column, final double value) {
		final double oldValue = getElement(row, column);
		setElement(row, column, value + oldValue);

		return this;
	}
	
	public IMatrixRead freeze();

	@Override
	public IVector getRowVector(final int index);

	public default void setRowVector(final int index, final IVectorRead row) {
		getRowVector(index).assign(row);
	}

}
