package utils;

public class IntUtils {
	
	private static final double INV_LOG_2 = 1.0 / Math.log(2);
	
	/**
	 * Returns ceil (log2(x))
	 * Result is undefined for 0.
	 * @param x
	 * @return ceil (log2(x))
	 */
	public static int ceilLog (final int x) {
		return Integer.SIZE - Integer.numberOfLeadingZeros(x - 1);
	}
	
	/**
	 * Returns ceil (log2(x))
	 * Result is undefined for 0.
	 * @param x
	 * @return ceil (log2(x))
	 */
	public static int ceilLog (final long x) {
		return Long.SIZE - Long.numberOfLeadingZeros(x - 1);
	}
	
	/**
	 * Returns ceil (log2(x))
	 * @param x
	 * @return ceil (log2(x))
	 */
	public static int ceilLog (final double x) {
		final double log = INV_LOG_2 * Math.log(x);
		
		return (int) Math.ceil(log);
	}

	/**
	 * Returns ratio between 2 numbers rounded up.
	 * Both divisor and divident must be positive and their sum must be less than Integer.MAX_VALUE.
	 * @param divident
	 * @param divisor
	 * @return ceil (divident / divisor)
	 */
	public static int divideRoundUp(final int divident, final int divisor) {
		return (divident + divisor - 1) / divisor;
	}

	/**
	 * Returns ratio between 2 numbers rounded up.
	 * Both divisor and divident must be positive and their sum must be less than Long.MAX_VALUE.
	 * @param divident
	 * @param divisor
	 * @return ceil (divident / divisor)
	 */
	public static long divideRoundUp(final long divident, final long divisor) {
		return (divident + divisor - 1) / divisor;
	}

	public static String intToStringWithSignum(final int x) {
		if (x > 0)
			return "+" + Integer.toString(x);
		else
			return Integer.toString(x);
	}

	/**
	 * Returns mask with the lowest count bits set.
	 * @param count count of bits
	 * @return mask
	 */
	public static long getLowestBitsLong (final int count) {
		return ((1L << count) - 1) |   // Works for count 0-63
				~((count >>> 6) - 1);   // Fixes count 64
	}
}
