package math;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.DoubleBinaryOperator;

public class Matrices {

	private static IMatrixRead applyToElementsBinaryOneNonzeroDense(final IMatrixRead a, final IMatrixRead b, final DoubleBinaryOperator operator) {
		final int rowCount = a.getRowCount();
		final int columnCount = a.getColumnCount();

		final IVectorRead[] result = new IVectorRead[rowCount];

		for (int row = 0; row < rowCount; row++)
			result[row] = BinaryVectorAlgorithmOneNonzero.getInstance().processElements(a.getRowVector(row), b.getRowVector(row), operator, new VectorSetter()).getVector();

		return new DenseMatrix(rowCount, columnCount, result);
	}

	private static IMatrixRead applyToElementsBinaryOneNonzeroSparse(final IMatrixRead a, final IMatrixRead b, final DoubleBinaryOperator operator) {
		final int rowCount = a.getRowCount();
		final int columnCount = a.getColumnCount();
		final IVectorRead emptyRow = Vectors.getZeroVector(columnCount);

		IMatrixRowIterator itA = a.getNonZeroRowIterator();

		IMatrixRowIterator itB = b.getNonZeroRowIterator();

		final SortedMap<Integer, IVector> rows = new TreeMap<>();

		while (itA.isValid() && itB.isValid()) {
			final int rowIndexA = itA.getRowIndex();
			final int rowIndexB = itB.getRowIndex();
			final int rowIndex = Math.min(rowIndexA, rowIndexB);

			IVectorRead rowA;
			IVectorRead rowB;

			if (rowIndexA <= rowIndexB) {
				rowA = itA.getRow();
				itA.next();
			}
			else
				rowA = emptyRow;

			if (rowIndexB <= rowIndexA) {
				rowB = itB.getRow();
				itB.next();
			}
			else
				rowB = emptyRow;

			final IVector result = BinaryVectorAlgorithmOneNonzero.getInstance().processElements(rowA, rowB, operator, new VectorSetter()).getMutableVector();
			rows.put(rowIndex, result);
		}

		while (itA.isValid()) {
			final int rowIndex = itA.getRowIndex();
			final IVectorRead rowA = itA.getRow();

			final IVector result = BinaryVectorAlgorithmOneNonzero.getInstance().processElements(rowA, emptyRow, operator, new VectorSetter()).getMutableVector();
			rows.put(rowIndex, result);
			itA.next();
		}

		while (itB.isValid()) {
			final int rowIndex = itB.getRowIndex();
			final IVectorRead rowB = itB.getRow();

			final IVector result = BinaryVectorAlgorithmOneNonzero.getInstance().processElements(emptyRow, rowB, operator, new VectorSetter()).getMutableVector();
			rows.put(rowIndex, result);
			itB.next();
		}

		return new SparseMatrix(rowCount, columnCount, rows);
	}
	
	public static IMatrixRead applyToElementsBinaryOneNonzero (final IMatrixRead a, final IMatrixRead b, final DoubleBinaryOperator operator) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);

		final int rowCount = a.getRowCount();
		final int columnCount = a.getColumnCount();

		if (rowCount != b.getRowCount() || columnCount != b.getColumnCount())
			throw new RuntimeException("Dimensions of input matrices does not match");

		if (a.density() == Density.SPARSE && b.density() == Density.SPARSE)
			return applyToElementsBinaryOneNonzeroSparse(a, b, operator);
		else
			return applyToElementsBinaryOneNonzeroDense(a, b, operator);
	}
	
	private static IVector[] createMatrixRows (final Density density, final int rowCount, final int columnCount) {
		final IVector[] rows = new IVector[rowCount];
		
		for (int i = 0; i < rowCount; i++)
			rows[i] = Vectors.vectorWithDensity(density, columnCount);
		
		return rows;
	}
	
	/**
	 * Adds matrix b into matrix a - calculates a += b.
	 * @param a target matrix
	 * @param b added matrix
	 */
	public static void addInPlace (final IMatrix a, final IMatrixRead b) {
		final int rowCount = a.getRowCount();

		if (rowCount != b.getRowCount() || a.getColumnCount() != b.getColumnCount())
			throw new RuntimeException("Different matrix sizes");

		for (IMatrixRowIterator rowIt = b.getNonZeroRowIterator(); rowIt.isValid(); rowIt.next())
			Vectors.addInPlace(a.getRowVector(rowIt.getRowIndex()), rowIt.getRow());
	}

	/**
	 * Returns mutable identity matrix with given dimension.
	 * @param dimension dimension of the matrix
	 * @return identity matrix
	 */
	private static IMatrix getMutableIdentityMatrix(final int dimension) {
		final IMatrix result = createMutableMatrix(Density.SPARSE, dimension, dimension);
		
		for (int i = 0; i < dimension; i++)
			result.setElement(i, i, 1.0);
		
		return result;
	}

	/**
	 * Returns immutable identity matrix with given dimension.
	 * @param dimension dimension of the matrix
	 * @return identity matrix
	 */
	public static IMatrixRead getIdentityMatrix (final int dimension) {
		return getMutableIdentityMatrix(dimension).freeze();
	}

	/**
	 * Creates mutable matrix with given dimensions and density.
	 * @param density density of the matrix
	 * @param rowCount number of rows
	 * @param columnCount number of columns
	 * @return mutable matrix
	 */
	public static IMatrix createMutableMatrix (final Density density, final int rowCount, final int columnCount) {
		switch (density) {
			case SPARSE:
				return new SparseMatrix(rowCount, columnCount);

			case DENSE:
				return new DenseMatrix(rowCount, columnCount, createMatrixRows(density, rowCount, columnCount));

			default:
				throw new RuntimeException("Unknown density: " + density);
		}
	}

	/**
	 * Returns immutable zero matrix.
	 * @param rowCount number of rows
	 * @param columnCount number of columns
	 * @return zero matrix
	 */
	public static IMatrixRead getZeroMatrix (final int rowCount, final int columnCount) {
		return new SparseMatrix(rowCount, columnCount).freeze();
	}

	/**
	 * Returns immutable diagonal matrix.
	 * @param diagonalVector vector with elements that should be on diagonal
	 * @return diagonal matrix
	 */
	public static IMatrixRead getDiagonalMatrix (final IVectorRead diagonalVector) {
		final int dimension = diagonalVector.getDimension();
		final IMatrix result = createMutableMatrix(Density.SPARSE, dimension, dimension);
		
		for (int row = 0; row < dimension; row++)
			result.setElement(row, row, diagonalVector.getElement(row));
		
		return result.freeze();
	}

	public static Density minDensity (final Density a, final Density b) {
		return (a == Density.SPARSE && b == Density.SPARSE) ? Density.SPARSE : Density.DENSE;
	}

	/**
	 * Returns matrix from given column vector.
	 * @param v column vector
	 * @return matrix with one column equal to given vector.
	 */
	public static IMatrixRead fromColumnVector (final IVectorRead v) {
		final IMatrix m = Matrices.createMutableMatrix(v.density(), v.getDimension(), 1);

		for (IVectorIterator it = v.getNonZeroElementIterator(); it.isValid(); it.next())
			m.setElement(it.getIndex(), 0, it.getElement());

		return m.freeze();
	}


}
