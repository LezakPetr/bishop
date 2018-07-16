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
	

	public static void swapArrayItems(final double[] array, final int indexA, final int indexB) {
		final double pom = array[indexA];
		
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

	/**
	 * Rounds the number to long.
	 * Rounding is done to the closest long, ties are rounded out of zero.
	 * @param value
	 * @return rounded value
	 */
	private static long round (final double value) {
		final long signum = (long) Math.signum(value);
		final double abs = Math.abs(value);
		
		return signum * Math.round(abs);
	}
	
	/**
	 * Rounds the number to integer. Throws an exception if int range is exceeded.
	 * Rounding is done to the closest int, ties are rounded out of zero.
	 * @param value
	 * @return rounded value
	 */
	public static int roundToInt (final double value) {
		final long roundedToLong = round (value);
		final int roundedToInt = (int) roundedToLong;
		
		if (roundedToLong != roundedToInt)
			throw new RuntimeException("Value out of range: " + value);
		
		return roundedToInt;
	}
	
	/**
	 * Rounds the number to short. Throws an exception if short range is exceeded.
	 * Rounding is done to the closest short, ties are rounded out of zero.
	 * @param value
	 * @return rounded value
	 */
	public static short roundToShort (final double value) {
		final long roundedToLong = round (value);
		final short roundedToShort = (short) roundedToLong;
		
		if (roundedToLong != roundedToShort)
			throw new RuntimeException("Value out of range: " + value);
		
		return roundedToShort;
	}
	
	/**
	 * Rounds the number to byte. Throws an exception if byte range is exceeded.
	 * Rounding is done to the closest byte, ties are rounded out of zero.
	 * @param value
	 * @return rounded value
	 */
	public static byte roundToByte (final double value) {
		final long roundedToLong = round (value);
		final byte roundedToByte = (byte) roundedToLong;
		
		if (roundedToLong != roundedToByte)
			throw new RuntimeException("Value out of range: " + value);
		
		return roundedToByte;
	}

	public static int roundToPercents(final double value) {
		return roundToInt(100.0 * value);
	}

	/**
	 * Returns square of given number.
	 * @param x number
	 * @return x^2
	 */
	public static double sqr (final double x) {
		return x * x;
	}
	
}
