package math;

import java.util.Arrays;

/**
 * Dense matrix is matrix with high numbers of non-zero elements.
 */
public class DenseMatrix extends MatrixImpl {
	private final IVector[] rows;

	/**
	 * Creates matrix from given array of rows.
	 *
	 * @param rowCount row count
	 * @param columnCount column count
	 * @param rows array with rows, the array and rows are taken into the matrix
	 */
	protected DenseMatrix(final int rowCount, final int columnCount, final IVector[] rows) {
		super(rowCount, columnCount);

		checkDensityOfROws(Arrays.asList(rows), Density.DENSE);
		this.rows = rows;
	}

	/**
	 * Creates matrix from given array of rows.
	 *
	 * @param rowCount row count
	 * @param columnCount column count
	 * @param rows array with rows; the rows may be taken or copied into the matrix
	 */
	protected DenseMatrix(final int rowCount, final int columnCount, final IVectorRead[] rows) {
		super(rowCount, columnCount);

		checkDensityOfROws(Arrays.asList(rows), Density.DENSE);

		this.rows = new IVector[rowCount];

		for (int i = 0; i < rowCount; i++) {
			final IVectorRead origRow = rows[i];

			if (origRow instanceof IVector)
				this.rows[i] = (IVector) origRow;
			else
				this.rows[i] = origRow.copy();

			this.rows[i].freeze();
		}

		this.frozen = true;
	}

	@Override
	public IVector getRowVector(final int index) {
		return rows[index];
	}

	@Override
	public IMatrixRead freeze() {
		this.frozen = true;

		for (IVector row : rows)
			row.freeze();

		return this;
	}

	@Override
	public IMatrix copy() {
		final IVector[] copyRows = new IVector[rowCount];

		for (int i = 0; i < rowCount; i++)
			copyRows[i] = rows[i].copy();

		return new DenseMatrix(rowCount, columnCount, copyRows);
	}

	@Override
	public Density density() {
		return Density.DENSE;
	}

	@Override
	public IMatrixRowIterator getNonZeroRowIterator() {
		return new IMatrixRowIterator() {
			int rowIndex;

			@Override
			public void next() {
				rowIndex++;
			}

			@Override
			public boolean isValid() {
				return rowIndex < rowCount;
			}

			@Override
			public int getRowIndex() {
				return rowIndex;
			}

			@Override
			public IVectorRead getRow() {
				return rows[rowIndex];
			}
		};
	}
}
