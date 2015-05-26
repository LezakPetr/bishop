package bisGui.math;

public interface IMatrix {
	
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
}
