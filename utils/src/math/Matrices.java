package math;


public class Matrices {
	
	/**
	 * Adds two matrices.
	 * @param a matrix
	 * @param b matrix
	 * @return a + b
	 */
	public static IMatrix plus (final IMatrix a, final IMatrix b) {
		final int rowCount = a.getRowCount();
		final int columnCount = a.getColumnCount();
		
		if (rowCount != b.getRowCount() || columnCount != b.getColumnCount())
			throw new RuntimeException("Dimensions of input matrices does not match");

		final double[][] elements = new double[rowCount][columnCount];
		
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++)
				elements[row][column] = a.getElement(row, column) + b.getElement(row, column);
		}
		
		return new MatrixImpl (elements);
	}
	
	/**
	 * Subtracts two matrices.
	 * @param a matrix
	 * @param b matrix
	 * @return a - b
	 */
	public static IMatrix minus (final IMatrix a, final IMatrix b) {
		final int rowCount = a.getRowCount();
		final int columnCount =a.getColumnCount();
		
		if (rowCount != b.getRowCount() || columnCount != b.getColumnCount())
			throw new RuntimeException("Dimensions of input matrices does not match");

		final double[][] elements = new double[rowCount][columnCount];
		
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++)
				elements[row][column] = a.getElement(row, column) - b.getElement(row, column);
		}
		
		return new MatrixImpl (elements);
	}

	public static IMatrix getIdentityMatrix (final int dimension) {
		final double[][] elements = Utils.createIdentityMatrix(dimension);
		
		return new MatrixImpl(elements);
	}
	
	public static IMatrix getDiagonalMatrix (final IVector diagonalVector) {
		final int dimension = diagonalVector.getDimension();
		final double[][] elements = new double[dimension][dimension];   // Elements initialized to 0.0
		
		for (int row = 0; row < dimension; row++)
			elements[row][row] = diagonalVector.getElement(row);
		
		return new MatrixImpl (elements);
	}

	/**
	 * Returns inverse matrix to given one.
	 * @param m matrix
	 * @return inverse matrix
	 */
	public static IMatrix inverse (final IMatrix m) {
		final int dimension = m.getRowCount();
		
		if (m.getColumnCount() != dimension)
			throw new RuntimeException("Matrix is not square matrix");
		
		// Get elements of original matrix
		final double[][] orig = new double[dimension][dimension];
		 
		for (int row = 0; row < dimension; row++) {
			for (int column = 0; column < dimension; column++)
				orig[row][column] = m.getElement(row, column);
		}
		
		// Prepare inverse matrix - initialize it by identity matrix
		final double[][] inverse = Utils.createIdentityMatrix(dimension);
		
		// Gaussian elimination - create triangular matrix
		for (int i = 0; i < dimension-1; i++) {
			// Find pivot in column 'i'
			int pivotRow = i;
			
			for (int row = i+1; row < dimension; row++) {
				if (Math.abs (orig[row][i]) > Math.abs (orig[pivotRow][i]))
					pivotRow = row;
			}
			
			// Swap pivot row and row 'i'
			Utils.swapArrayItems(orig, pivotRow, i);
			Utils.swapArrayItems(inverse, pivotRow, i);
			
			// Elimination
			final double divider = orig[i][i];
			
			if (divider == 0)
				throw new RuntimeException("Matrix is singular");
			
			final int origOffset = i;
			final int origCount = dimension - origOffset;
			
			for (int row = i+1; row < dimension; row++) {
				if (orig[row][i] != 0.0) {
					final double coeff = -orig[row][i] / divider;
					
					Utils.inPlaceCombineVectors (orig[i], origOffset, orig[row], origOffset, origCount, coeff, 1.0);
					Utils.inPlaceCombineVectors (inverse[i], 0, inverse[row], 0, dimension, coeff, 1.0);
				}
			}
		}
		
		// Divide rows by diagonal elements
		for (int row = 0; row < dimension; row++) {
			final double coeff = 1.0 / orig[row][row];
			
			for (int column = row; column < dimension; column++)
				orig[row][column] *= coeff;
			
			for (int column = 0; column < dimension; column++)
				inverse[row][column] *= coeff;
		}
		
		// Eliminate elements above diagonal
		for (int i = dimension - 1; i >= 0; i--) {
			for (int row = 0; row < i; row++) {
				final double coeff = -orig[row][i];
				
				Utils.inPlaceCombineVectors (orig[i], i, orig[row], i, dimension-i, coeff, 1.0);
				Utils.inPlaceCombineVectors (inverse[i], 0, inverse[row], 0, dimension, coeff, 1.0);
			}
		}
		
		return new MatrixImpl(inverse);
	}

	/**
	 * Multiplies two matrices .
	 * @param a input matrix
	 * @param b input matrix
	 * @return matrix a*b
	 */
	public static IMatrix multiply (final IMatrix a, final IMatrix b) {
		final int innerCount = a.getColumnCount();
		
		if (innerCount != b.getRowCount())
			throw new RuntimeException("Bad dimensions");
		
		final int rowCount = a.getRowCount();
		final int columnCount = b.getColumnCount();
		
		final double[][] result = new double[rowCount][columnCount];
		
		for (int row = 0; row < rowCount; row++) {
			for (int column = 0; column < columnCount; column++) {
				double sum = 0.0;
				
				for (int i = 0; i < innerCount; i++)
					sum += a.getElement(row, i) * b.getElement(i, column);
				
				result[row][column] = sum;
			}
		}
		
		return new MatrixImpl (result);
	}

	/**
	 * Multiplies given vector by given matrix.
	 * @param v input vector
	 * @param m input matrix
	 * @return vector v*m
	 */
	public static IVector multiply (final IVector v, final IMatrix m) {
		final int rowCount = m.getRowCount();
		
		if (rowCount != v.getDimension())
			throw new RuntimeException("Bad dimensions");
		
		final int columnCount = m.getColumnCount();
		final double[] result = new double[columnCount];
		
		for (int column = 0; column < columnCount; column++) {
			double sum = 0.0;
			
			for (int row = 0; row < rowCount; row++)
				sum += v.getElement(row) * m.getElement (row, column);
			
			result[column] =  sum;
		}
		
		return new VectorImpl (result);
	}
	
	/**
	 * Returns maximal absolute element in the matrix.
	 * @param matrix matrix
	 * @return maximal absolute element
	 */
	public static final double maxAbsElement (final IMatrix matrix) {
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
	
}
