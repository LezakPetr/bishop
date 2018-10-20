package math;

import java.util.Collection;

abstract public class MatrixImpl implements IMatrix {

	protected final int rowCount;
	protected final int columnCount;
	protected boolean frozen;
	
	/**
	 * Creates matrix from given array.
	 * First index of array is row, second is column.
	 * @param rowCount row count
	 * @param columnCount column count
	 */
	protected MatrixImpl (final int rowCount, final int columnCount) {
		this.rowCount = rowCount;
		this.columnCount = columnCount;
	}

	protected static void checkDensityOfROws(final Collection<? extends IVectorRead> rows, final Density expectedDensity) {
		for (IVectorRead row: rows) {
			if (row.density() != expectedDensity)
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
		return this.getRowVector(row).getElement(column);
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
		
		this.getRowVector(row).setElement(column, value);
		
		return this;
	}

	protected void checkNotFrozen() {
		if (frozen)
			throw new RuntimeException("Matrix is frozen");
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
		
		for (int i = 0; i < rowCount; i++)
			hash += 31 * getRowVector(i).hashCode();
		
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

	@Override
	public IMatrixRead transpose() {
		final IMatrix result = Matrices.createMutableMatrix(density(), getColumnCount(), getRowCount());

		for (IMatrixRowIterator rowIt = getNonZeroRowIterator(); rowIt.isValid(); rowIt.next()) {
			for (IVectorIterator it = rowIt.getRow().getNonZeroElementIterator(); it.isValid(); it.next()) {
				result.setElement(it.getIndex(), rowIt.getRowIndex(), it.getElement());
			}
		}

		return result.freeze();
	}

	@Override
	public boolean isZero() {
		for (IMatrixRowIterator rowIt = getNonZeroRowIterator(); rowIt.isValid(); rowIt.next()) {
			if (!rowIt.getRow().isZero())
				return false;
		}

		return true;
	}


}
