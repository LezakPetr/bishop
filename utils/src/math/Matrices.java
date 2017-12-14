package math;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;

public class Matrices {
	
	public static IMatrixRead applyToElementsBinaryOneNonzero (final IMatrixRead a, final IMatrixRead b, final DoubleBinaryOperator operator) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);
		
		final int rowCount = a.getRowCount();
		final int columnCount = a.getColumnCount();
		
		if (rowCount != b.getRowCount() || columnCount != b.getColumnCount())
			throw new RuntimeException("Dimensions of input matrices does not match");

		final IVectorRead[] result = new IVectorRead[rowCount];
		
		for (int row = 0; row < rowCount; row++)
			result[row] = Vectors.processElementsBinaryOneNonzero(a.getRowVector(row), b.getRowVector(row), operator, new VectorSetter()).getVector();
		
		return new MatrixImpl(rowCount, columnCount, result);
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
		return applyToElementsBinaryOneNonzero(a, b, Double::sum);
	}
	
	/**
	 * Subtracts two matrices.
	 * @param a matrix
	 * @param b matrix
	 * @return a - b
	 */
	public static IMatrixRead minus (final IMatrixRead a, final IMatrixRead b) {
		return applyToElementsBinaryOneNonzero(a, b, (x, y) -> x - y);
	}

	private static IMatrix getMutableIdentityMatrix(int dimension) {
		final IMatrix result = createMutableMatrix(Density.SPARSE, dimension, dimension);
		
		for (int i = 0; i < dimension; i++)
			result.setElement(i, i, 1.0);
		
		return result;
	}

	public static IMatrixRead getIdentityMatrix (final int dimension) {
		return getMutableIdentityMatrix(dimension).freeze();
	}
	
	public static IMatrix createMutableMatrix (final Density density, final int rowCount, final int columnCount) {
		final IVector[] rows = createMatrixRows(density, rowCount, columnCount);
		
		return new MatrixImpl(rowCount, columnCount, rows);
	}
	
	public static IMatrixRead getDiagonalMatrix (final IVectorRead diagonalVector) {
		final int dimension = diagonalVector.getDimension();
		final IMatrix result = createMutableMatrix(Density.SPARSE, dimension, dimension);
		
		for (int row = 0; row < dimension; row++)
			result.setElement(row, row, diagonalVector.getElement(row));
		
		return result.freeze();
	}

	/**
	 * Returns inverse matrix to given one.
	 * @param m matrix
	 * @return inverse matrix
	 */
	public static IMatrixRead inverse (final IMatrixRead m) {
		final int dimension = m.getRowCount();
		
		if (m.getColumnCount() != dimension)
			throw new RuntimeException("Matrix is not square matrix");
		
		// Get elements of original matrix
		final IMatrix orig = m.copy();
		
		// Prepare inverse matrix - initialize it by identity matrix
		final IMatrix inverse = getMutableIdentityMatrix(dimension);
		
		// Gaussian elimination - create triangular matrix
		for (int i = 0; i < dimension-1; i++) {
			// Find pivot in column 'i'
			int pivotRow = i;
			
			for (int row = i+1; row < dimension; row++) {
				if (Math.abs (orig.getElement(row, i)) > Math.abs (orig.getElement(pivotRow, i)))
					pivotRow = row;
			}
			
			// Swap pivot row and row 'i'
			Vectors.swap(orig.getRowVector(pivotRow), orig.getRowVector(i));
			Vectors.swap(inverse.getRowVector(pivotRow), inverse.getRowVector(i));
			
			// Elimination
			final double divider = orig.getElement(i, i);
			
			if (divider == 0)
				throw new RuntimeException("Matrix is singular");
			
			for (int row = i+1; row < dimension; row++) {
				if (orig.getElement(row, i) != 0.0) {
					final double coeff = -orig.getElement(row, i) / divider;
					
					if (coeff != 0) {
						orig.setRowVector (row, Vectors.multiplyAndAdd (orig.getRowVector(row), orig.getRowVector(i), coeff));
						inverse.setRowVector (row, Vectors.multiplyAndAdd (inverse.getRowVector(row), inverse.getRowVector(i), coeff));
					}
				}
			}
		}
		
		// Divide rows by diagonal elements
		for (int row = 0; row < dimension; row++) {
			final double coeff = 1.0 / orig.getElement(row, row);
			
			for (int column = row; column < dimension; column++)
				orig.setElement(row, column, orig.getElement(row, column) * coeff);
			
			for (int column = 0; column < dimension; column++)
				inverse.setElement(row, column, inverse.getElement(row, column) * coeff);
		}
		
		// Eliminate elements above diagonal
		for (int i = dimension - 1; i >= 0; i--) {
			for (int row = 0; row < i; row++) {
				final double coeff = -orig.getElement(row, i);
				
				if (coeff != 0) {
					orig.setRowVector (row, Vectors.multiplyAndAdd (orig.getRowVector(row), orig.getRowVector(i), coeff));
					inverse.setRowVector (row, Vectors.multiplyAndAdd (inverse.getRowVector(row), inverse.getRowVector(i), coeff));
				}
			}
		}
		
		return inverse.freeze();
	}

	/**
	 * Multiplies two matrices .
	 * @param a input matrix
	 * @param b input matrix
	 * @return matrix a*b
	 */
	public static IMatrixRead multiply (final IMatrixRead a, final IMatrixRead b) {
		final int innerCount = a.getColumnCount();
		
		if (innerCount != b.getRowCount())
			throw new RuntimeException("Bad dimensions");
		
		final int rowCount = a.getRowCount();
		final int columnCount = b.getColumnCount();
		
		final Density density = minDensity(a.density(), b.density());
		final IMatrix result = createMutableMatrix(density, rowCount, columnCount);
		
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				final double dotProduct = Vectors.dotProduct (a.getRowVector(row), b.getColumnVector (column));
				
				result.setElement(row, column, dotProduct);
			}
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
		final Density density = minDensity(v.density(), m.density());
		final IVector result = Vectors.vectorWithDensity(density, rowCount);
		
		for (int row = 0; row < rowCount; row++) {
			final double dotProduct = Vectors.dotProduct (v, m.getRowVector(row));
			result.setElement(row, dotProduct);
		}
		
		return result.freeze();
	}

	
	/**
	 * Returns maximal absolute element in the matrix.
	 * @param matrix matrix
	 * @return maximal absolute element
	 */
	public static double maxAbsElement (final IMatrixRead matrix) {
		final int rowCount = matrix.getRowCount();
		final int columnCount = matrix.getColumnCount();

		double maxAbsElement = 0.0;

		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				final double absElement = Math.abs(matrix.getElement(row, column));
				
				maxAbsElement = Math.max(maxAbsElement, absElement);
			}
		}

		return maxAbsElement;
	}
	
	public static IMatrixRead transpose (final IMatrixRead matrix) {
		final IMatrix result = createMutableMatrix(matrix.density(), matrix.getColumnCount(), matrix.getRowCount());
		
		for (int i = 0; i < matrix.getRowCount(); i++) {
			for (IVectorIterator it = matrix.getRowVector(i).getNonZeroElementIterator(); it.isValid(); it.next()) {
				result.setElement(it.getIndex(), i, it.getElement());
			}
		}
		
		return result.freeze();
	}

	public static IMatrixRead multiplyRows(final IMatrixRead m, final IVectorRead v) {
		final int rowCount = m.getRowCount();
		
		if (rowCount != v.getDimension())
			throw new RuntimeException("Bad dimensions");
		
		final int columnCount = m.getColumnCount();
		final IVectorRead[] result = new IVector[rowCount];
		
		for (int row = 0; row < rowCount; row++) {
			result[row] = Vectors.multiply(v.getElement(row), m.getRowVector(row));
		}
		
		return new MatrixImpl(rowCount, columnCount, result).freeze();
	} 
	
}
