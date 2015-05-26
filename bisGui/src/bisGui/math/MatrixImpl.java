package bisGui.math;

public class MatrixImpl implements IMatrix {

	private final int rowCount;
	private final int columnCount;
	private final double[][] elements;
	
	/**
	 * Creates matrix from given array.
	 * First index of array is row, second is column.
	 * Array is not copied. Changing it is forbidden.
	 * @param elements array of elements
	 */
	public MatrixImpl (final double[][] elements) {
		if (elements != null) {
			this.rowCount = elements.length;
			
			if (this.rowCount > 0) {
				this.columnCount = elements[0].length;
				
				for (int i = 1; i < this.rowCount; i++) {
					if (elements[i].length != this.columnCount)
						throw new RuntimeException("Bad size of components");
				}
			}
			else {
				// Matrix without any row has no zero columns
				this.columnCount = 0;
			}
		}
		else {
			// Zero-sized matrix
			this.rowCount = 0;
			this.columnCount = 0;
		}
		
		// Copy content
		this.elements = new double[rowCount][columnCount];
		
		assignContent(elements);
	}

	private void assignContent(final double[][] orig) {
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++)
				this.elements[row][column] = orig[row][column];
		}
	}
	
	/**
	 * Returns number of rows.
	 * @return number of rows
	 */
	public int getRowCount() {
		return rowCount;
	}
	
	/**
	 * Returns number of columns.
	 * @return number of columns
	 */
	public int getColumnCount() {
		return columnCount;
	}
	
	/**
	 * Returns element on given row and column.
	 * @param row row index
	 * @param column column index
	 * @return element on given row and column
	 */
	public double getElement (final int row, final int column) {
		return elements[row][column];
	}
}
