package math;

import utils.IntUtils;

import java.util.*;

/**
 * Matrix that has constant (relatively small) number of non-zero elements.
 */
public class SparseMatrix extends MatrixImpl {
	private static final int[] ZERO_ROW_INDICES = {};
	private static final IVector[] ZERO_ROWS = {};

	// List of non-zero rows. The rows are stored in ascending order.
	// Row indices are stored in array rowIndices, rows in array rows.
	// Only first nonZeroElementCount rows are valid, the matrix preallocates bigger arrays
	// to minimize reallocations.
	private int[] rowIndices;
	private IVector[] rows;
	private int nonZeroElementCount;

	// Precreated frozen zero row. This row is returned if non-stored (so zero) row is queried
	// in frozen matrix.
	private final IVector zeroRow;

	/**
	 * Creates sparse matrix with given rows.
	 * @param rowCount row count
	 * @param columnCount column count
	 * @param rows non-zero rows
	 */
	protected SparseMatrix (final int rowCount, final int columnCount, final SortedMap<Integer, IVector> rows) {
		this (rowCount, columnCount);

		checkDensityOfROws(rows.values(), Density.SPARSE);

		nonZeroElementCount = rows.size();

		this.rowIndices = new int[nonZeroElementCount];
		this.rows = new IVector[nonZeroElementCount];

		int index = 0;

		for (Iterator<Map.Entry<Integer, IVector>> it = rows.entrySet().iterator(); it.hasNext(); ) {
			final Map.Entry<Integer, IVector> entry = it.next();

			this.rowIndices[index] = entry.getKey();
			this.rows[index] = entry.getValue();
			index++;
		}

		assert nonZeroElementCount == index;
	}

	/**
	 * Creates empty sparse matrix.
	 * @param rowCount row count
	 * @param columnCount column count
	 */
	protected SparseMatrix (final int rowCount, final int columnCount) {
		super (rowCount, columnCount);

		this.zeroRow = new SparseVector(columnCount);
		this.zeroRow.freeze();
		this.rowIndices = ZERO_ROW_INDICES;
		this.rows = ZERO_ROWS;
	}

	/**
	 * Finds sparse index (index into rowIndices and rows array).
	 * The method works with log (nonZeroElementCount) at maximum, but finding insertion
	 * point at the end of the list works in constant time. This ensures that the matrix
	 * can be filled in ascending order of rows in linear time.
	 * @param index row index
	 * @return sparse index in case that the row is stored in the matrix,
	 *         -insertionPoint - 1  in case that the row is not stored in the matrix
	 */
	private int findSparseIndex(final int index) {
		if (nonZeroElementCount == rowCount)
			return index;   // Optimization for the case that there is not any zero vector

		return IntUtils.sortedArraySearch(rowIndices, nonZeroElementCount, index);
	}

	/**
	 * Returns row with given index. If the row is not stored in the matrix and
	 * if the matrix is not frozen the row is created.
	 * @param index row index
	 * @return row
	 */
	@Override
	public IVector getRowVector(final int index) {
		final int sparseIndex = findSparseIndex(index);

		if (sparseIndex >= 0)
			return rows[sparseIndex];

		if (frozen)
			return zeroRow;
		else {
			final IVector row = Vectors.sparse(columnCount);
			addRow (-sparseIndex - 1, index, row);

			return row;
		}
	}

	/**
	 * Adds row at given sparse index.
	 * @param sparseIndex sparse index of the inserted row
	 * @param rowIndex index of the row
	 * @param rowToInsert row to insert
	 */
	private void addRow(final int sparseIndex, final int rowIndex, final IVector rowToInsert) {
		final IVector[] origRows = rows;
		final int[] origIndices = rowIndices;

		final int origSize = nonZeroElementCount;
		nonZeroElementCount++;

		if (rows.length < nonZeroElementCount) {
			final int newSize = Math.min(Math.max(nonZeroElementCount, 2*rows.length), rowCount);

			rows = new IVector[newSize];
			System.arraycopy(origRows, 0, rows, 0, sparseIndex);

			rowIndices = new int[newSize];
			System.arraycopy(origIndices, 0, rowIndices, 0, sparseIndex);
		}

		System.arraycopy(origRows, sparseIndex, rows, sparseIndex + 1, origSize - sparseIndex);
		rows[sparseIndex] = rowToInsert;

		System.arraycopy(origIndices, sparseIndex, rowIndices, sparseIndex + 1, origSize - sparseIndex);
		rowIndices[sparseIndex] = rowIndex;
	}

	/**
	 * Freezes the matrix and all of its rows.
	 * @return this
	 */
	@Override
	public IMatrixRead freeze() {
		this.frozen = true;

		for (int sparseIndex = 0; sparseIndex < nonZeroElementCount; sparseIndex++)
			rows[sparseIndex].freeze();

		return this;
	}

	/**
	 * Returns mutable copy of the matrix.
	 * @return copy
	 */
	@Override
	public IMatrix copy() {
		final SparseMatrix result = new SparseMatrix(rowCount, columnCount);
		result.nonZeroElementCount = this.nonZeroElementCount;
		result.rowIndices = Arrays.copyOf(this.rowIndices, nonZeroElementCount);
		result.rows = new IVector[nonZeroElementCount];

		for (int sparseIndex = 0; sparseIndex < nonZeroElementCount; sparseIndex++)
			result.rows[sparseIndex] = this.rows[sparseIndex].copy();

		return result;
	}

	@Override
	public Density density() {
		return Density.SPARSE;
	}

	@Override
	public IMatrixRowIterator getNonZeroRowIterator() {
		return new IMatrixRowIterator() {
			private int sparseIndex;

			@Override
			public void next() {
				sparseIndex++;
			}

			@Override
			public boolean isValid() {
				return sparseIndex < nonZeroElementCount;
			}

			@Override
			public int getRowIndex() {
				return rowIndices[sparseIndex];
			}

			@Override
			public IVectorRead getRow() {
				return rows[sparseIndex];
			}
		};
	}
}
