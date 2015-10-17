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
}
