package utils;

import java.util.concurrent.atomic.LongAdder;

public class RatioCalculator {

	private final LongAdder successCount = new LongAdder();
	private final LongAdder failCount = new LongAdder();
	
	/**
	 * Returns ratio between of successful invocations against all invocations.
	 * @return ratio or NaN in case of no invocation
	 */
	public double getRatio() {
		final long success = successCount.longValue();
		final long fail = failCount.longValue();
		final long total = fail + success;
		final double ratio = (double) success / (double) total;
		
		return ratio;
	}
	
	public void addInvocation (final boolean success) {
		if (success)
			successCount.increment();
		else
			failCount.increment();
	}
}
