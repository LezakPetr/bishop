package utils;

public class IntUtils {
	
	private static final double INV_LOG_2 = 1.0 / Math.log(2);
	
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
	
	/**
	 * Returns ceil (log2(x))
	 * @param x
	 * @return ceil (log2(x))
	 */
	public static int ceilLog (final double x) {
		final double log = INV_LOG_2 * Math.log(x);
		
		return (int) Math.ceil(log);
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
