package math;

public class MatrixImpl implements IMatrix {

	private final int rowCount;
	private final int columnCount;
	private final IVector[] rows;
	private boolean frozen;
	
	/**
	 * Creates matrix from given array.
	 * First index of array is row, second is column.
	 * @param rowCount row count
	 * @param columnCount column count
	 * @param elements array of elements
	 */
	protected MatrixImpl (final int rowCount, final int columnCount, final IVector[] rows) {
		checkSameDensityOfROws(rows);
		
		this.rowCount = rowCount;
		this.columnCount = columnCount;
		this.rows = rows;
	}
	
	/**
	 * Creates matrix from given array.
	 * First index of array is row, second is column.
	 * @param rowCount row count
	 * @param columnCount column count
	 * @param elements array of elements
	 */
	protected MatrixImpl (final int rowCount, final int columnCount, final IVectorRead[] rows) {
		checkSameDensityOfROws(rows);
		
		this.rowCount = rowCount;
		this.columnCount = columnCount;
		this.rows = new IVector[rowCount];
		
		for (int i = 0; i < rowCount; i++) {
			final IVectorRead origRow = rows[i];
			
			if (origRow instanceof IVector)
				this.rows[i] = (IVector) origRow;
			else
				this.rows[i] = Vectors.copy(origRow);
			
			this.rows[i].freeze();
		}
		
		this.frozen = true;
	}


	private void checkSameDensityOfROws(final IVectorRead[] rows) {
		if (rows.length == 0)
			return;
		
		final Density expectedDensity = rows[0].density();
		
		for (int i = 1; i < rows.length; i++) {
			if (rows[i].density() != expectedDensity)
				throw new RuntimeException("Matrix rows have different density");
		}
	}

	/**
	 * Returns number of rows.
	 * @return number of rows
	 */
	@Override
	public int getRowCount() {
		return rowCount;
	}
	
	/**
	 * Returns number of columns.
	 * @return number of columns
	 */
	@Override
	public int getColumnCount() {
		return columnCount;
	}
	
	/**
	 * Returns element on given row and column.
	 * @param row row index
	 * @param column column index
	 * @return element on given row and column
	 */
	@Override
	public double getElement (final int row, final int column) {
		return rows[row].getElement(column);
	}
	
	/**
	 * Sets element on given row and column.
	 * @param row row index
	 * @param column column index
	 * @value element on given row and column
	 */
	@Override
	public MatrixImpl setElement (final int row, final int column, final double value) {
		checkNotFrozen();
		
		this.rows[row].setElement(column, value);
		
		return this;
	}

	private void checkNotFrozen() {
		if (frozen)
			throw new RuntimeException("Matrix is frozen");
	}
	
	@Override
	public IMatrixRead freeze() {
		this.frozen = true;
		
		for (IVector row: rows)
			row.freeze();
		
		return this;
	}

	@Override
	public IMatrix copy() {
		final IVector[] copyRows = new IVector[rowCount];
		
		for (int i = 0; i < rowCount; i++)
			copyRows[i] = Vectors.copy(rows[i]);
		
		return new MatrixImpl(rowCount, columnCount, copyRows);
	}

	@Override
	public IVector getRowVector(final int index) {
		return rows[index];
	}

	@Override
	public Density density() {
		return (rowCount > 0) ? rows[0].density() : Density.SPARSE;
	}
	
	@Override
	public boolean equals (final Object obj) {
		if (!(obj instanceof IMatrix))
			return false;
		
		final IMatrix that = (IMatrix) obj;
		
		if (this.getRowCount() != that.getRowCount() || this.getColumnCount() != that.getColumnCount())
			return false;
		
		for (int i = 0; i < rowCount; i++) {
			if (!this.getRowVector(i).equals(that.getRowVector(i)))
				return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		
		for (IVectorRead row: rows)
			hash += 31 * row.hashCode();
		
		return hash;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("[");
		
		for (int row = 0; row < rowCount; row++) {
			if (row > 0)
				result.append("; ");

			for (int column = 0; column < columnCount; column++) {
				if (column > 0)
					result.append(", ");
				
				result.append(getElement(row, column));
			}
		}
		
		result.append("]");
		
		return result.toString();
	}

	@Override
	public boolean isImmutable() {
		return frozen;
	}

}
