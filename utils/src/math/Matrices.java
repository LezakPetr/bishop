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
	 * Adds two matrices.
	 * @param a matrix
	 * @param b matrix
	 * @return a + b
	 */
	public static IMatrixRead plus (final IMatrixRead a, final IMatrixRead b) {
		if (a.isZero())
			return a.immutableCopy();

		if (b.isZero())
			return a.immutableCopy();

		return applyToElementsBinaryOneNonzero(a, b, Double::sum);
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
	 * Subtracts two matrices.
	 * @param a matrix
	 * @param b matrix
	 * @return a - b
	 */
	public static IMatrixRead minus (final IMatrixRead a, final IMatrixRead b) {
		if (b.isZero())
			return a.immutableCopy();

		return applyToElementsBinaryOneNonzero(a, b, (x, y) -> x - y);
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

	/**
	 * Multiplies two matrices.
	 * @param a input matrix
	 * @param b input matrix
	 * @return matrix a*b
	 */
	public static IMatrixRead multiply (final IMatrixRead a, final IMatrixRead b) {
		final int innerCount = a.getColumnCount();
		
		if (innerCount != b.getRowCount())
			throw new RuntimeException("Bad dimensions of matrices");
		
		final int rowCount = a.getRowCount();
		final int columnCount = b.getColumnCount();
		
		final Density density = minDensity(a.density(), b.density());
		final IMatrix result = createMutableMatrix(density, rowCount, columnCount);

		for (IMatrixRowIterator it = a.getNonZeroRowIterator(); it.isValid(); it.next()) {
			final int rowIndex = it.getRowIndex();
			final IVectorRead row = it.getRow();
			final IVector resultRow = result.getRowVector(rowIndex);

			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				final double dotProduct = Vectors.dotProduct (row, b.getColumnVector (columnIndex));

				resultRow.setElement(columnIndex, dotProduct);
			}
		}
		
		return result.freeze();
	}

	/**
	 * Multiplies matrix with constant.
	 * @param c constant
	 * @param m matrix
	 * @return matrix c * m
	 */
	public static IMatrixRead multiply (final double c, final IMatrixRead m) {
		if (m.isZero())
			return m.immutableCopy();

		final int rowCount = m.getRowCount();
		final int columnCount = m.getColumnCount();

		final IMatrix result = createMutableMatrix(m.density(), rowCount, columnCount);

		for (IMatrixRowIterator rowIt = m.getNonZeroRowIterator(); rowIt.isValid(); rowIt.next()) {
			final IVector resultRow = result.getRowVector(rowIt.getRowIndex());

			for (IVectorIterator it = rowIt.getRow().getNonZeroElementIterator(); it.isValid(); it.next())
				resultRow.setElement(it.getIndex(), c * it.getElement());
		}

		return result.freeze();
	}
	
	private static Density minDensity (final Density a, final Density b) {
		return (a == Density.SPARSE && b == Density.SPARSE) ? Density.SPARSE : Density.DENSE;
	}

	/**
	 * Multiplies given vector by given matrix.
	 * @param v input vector
	 * @param m input matrix
	 * @return vector v*m
	 */
	public static IVectorRead multiply (final IVectorRead v, final IMatrixRead m) {
		final int rowCount = m.getRowCount();
		
		if (rowCount != v.getDimension())
			throw new RuntimeException("Bad dimensions");

		final int columnCount = m.getColumnCount();

		if (v.isZero() || m.isZero())
			return Vectors.getZeroVector(columnCount);

		final Density density = minDensity(v.density(), m.density());
		final IVector result = Vectors.vectorWithDensity(density, columnCount);
		
		for (int column = 0; column < columnCount; column++) {
			final double dotProduct = Vectors.dotProduct (v, m.getColumnVector (column));
			
			if (dotProduct != 0)
				result.setElement(column, dotProduct);
		}
		
		return result.freeze();
	}
	
	/**
	 * Multiplies given matrix by given vector.
	 * @param m input matrix
	 * @param v input vector
	 * @return vector m*v
	 */
	public static IVectorRead multiply (final IMatrixRead m, final IVectorRead v) {
		final int columnCount = m.getColumnCount();
		
		if (columnCount != v.getDimension())
			throw new RuntimeException("Bad dimensions");

		final int rowCount = m.getRowCount();

		if (m.isZero() || v.isZero())
			return Vectors.getZeroVector(rowCount);

		final Density density = minDensity(v.density(), m.density());
		final IVector result = Vectors.vectorWithDensity(density, rowCount);

		for (IMatrixRowIterator rowIt = m.getNonZeroRowIterator(); rowIt.isValid(); rowIt.next()) {
			final double dotProduct = Vectors.dotProduct (v, rowIt.getRow());
			result.setElement(rowIt.getRowIndex(), dotProduct);
		}
		
		return result.freeze();
	}


	/**
	 * Returns maximal absolute element in the matrix.
	 * @param matrix matrix
	 * @return maximal absolute element
	 */
	public static double maxAbsElement (final IMatrixRead matrix) {
		double maxAbsElement = 0.0;

		for (IMatrixRowIterator rowIt = matrix.getNonZeroRowIterator(); rowIt.isValid(); rowIt.next()) {
			for (IVectorIterator it = rowIt.getRow().getNonZeroElementIterator(); it.isValid(); it.next()) {
				final double absElement = Math.abs(it.getElement());
				maxAbsElement = Math.max(maxAbsElement, absElement);
			}
		}

		return maxAbsElement;
	}
	
	public static IMatrixRead transpose (final IMatrixRead matrix) {
		final IMatrix result = createMutableMatrix(matrix.density(), matrix.getColumnCount(), matrix.getRowCount());

		for (IMatrixRowIterator rowIt = matrix.getNonZeroRowIterator(); rowIt.isValid(); rowIt.next()) {
			for (IVectorIterator it = rowIt.getRow().getNonZeroElementIterator(); it.isValid(); it.next()) {
				result.setElement(it.getIndex(), rowIt.getRowIndex(), it.getElement());
			}
		}
		
		return result.freeze();
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
