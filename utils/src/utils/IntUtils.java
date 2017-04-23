package utils;

public class IntUtils {
	/**
	 * Returns ceil (log2(x))
	 * @param x
	 * @return ceil (log2(x))
	 */
	public static int ceilLog (final int x) {
		return Integer.SIZE - Integer.numberOfLeadingZeros(x - 1);
	}
	
	/**
	 * Returns ceil (log2(x))
	 * @param x
	 * @return ceil (log2(x))
	 */
	public static int ceilLog (final long x) {
		return Long.SIZE - Long.numberOfLeadingZeros(x - 1);
	}

	public static int divideRoundUp(final int divident, final int divisor) {
		return (divident + divisor - 1) / divisor;
	}

	public static String intToStringWithSignum(final int x) {
		if (x > 0)
			return "+" + Integer.toString(x);
		else
			return Integer.toString(x);
	}
}
