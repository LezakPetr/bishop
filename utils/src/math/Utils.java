package math;

public class Utils {
	
	/**
	 * Swaps two items of the array.
	 * @param <T> type of array item
	 * @param array array with swapped items
	 * @param indexA index of first swapped item
	 * @param indexB index of first swapped item
	 */
	public static <T> void swapArrayItems (final T[] array, final int indexA, final int indexB) {
		final T pom = array[indexA];
		
		array[indexA] = array[indexB];
		array[indexB] = pom;
	}
	
	/**
	 * Creates square matrix (2D array) with given dimension. Matrix will have ones on the diagonal and zeros elsewhere.
	 * @param dimension dimension of the matrix
	 * @return identity matrix
	 */
	public static double[][] createIdentityMatrix (final int dimension) {
		final double[][] matrix = new double[dimension][dimension];   // Elements initialized to 0.0
		 
		for (int row = 0; row < dimension; row++)
			matrix[row][row] = 1.0;
		
		return matrix;
	}

	public static void inPlaceCombineVectors(final double[] src, final int srcOffset, final double[] dest,
			final int destOffset, final int count, final double srcCoeff, final double destCoeff) {
		for (int i = 0; i < count; i++) {
			dest[destOffset+i] = src[srcOffset+i]*srcCoeff + dest[destOffset+i]*destCoeff;
		}
	}
	
	public static double linearInterpolation (final double x1, final double x2, final double y1, final double y2, final double x) {
		return y1 + (y2 - y1) / (x2 - x1) * (x - x1);
	}

	public static IVector solveEquationsLeastSquare(final IMatrix a, final IVector b, final IVector weights) {
		final IVector sqrtWeights = Vectors.applyToElements(weights, (x) -> Math.sqrt(x));
		final IVector normalizedWeights = Vectors.normalize (sqrtWeights);
		
		final IMatrix weightedA = Matrices.multiplyRows (a, normalizedWeights);
		final IMatrix at = Matrices.transpose(weightedA);
		
		final IMatrix m = Matrices.multiply(at, weightedA);
		final IMatrix invM = Matrices.inverse(m);
		
		final IVector updatedB = Matrices.multiply(at, Vectors.elementMultiply(b, normalizedWeights));
		
		return Matrices.multiply(invM, updatedB);
	}
}
