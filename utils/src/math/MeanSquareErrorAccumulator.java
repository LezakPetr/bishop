package math;

import java.util.Arrays;

public class MeanSquareErrorAccumulator implements IErrorAccumulator {
	private static final int OUTPUT_COUNT = 2;

	private final double[] totalErrors = new double[OUTPUT_COUNT];
	private final long[] sampleCounts = new long[OUTPUT_COUNT];

	public void addSample (final double predictedY, final int expectedY) {
		totalErrors[expectedY] += Utils.sqr(predictedY - expectedY);
		sampleCounts[expectedY]++;

		logErrors();
	}

	private void logErrors() {
		if ((Arrays.stream(sampleCounts).sum() & 0xFFFFF) == 0)
			System.out.println("MSE 0 = " + getMeanErrorForOutput(0) + ", MSE 1 = " + getMeanErrorForOutput(1) + ", MSE total = " + getMeanError());
	}

	public double getMeanError() {
		final double totalError = Arrays.stream(totalErrors).sum();
		final long sampleCount = Arrays.stream(sampleCounts).sum();
		return totalError / sampleCount;
	}

	public double getMeanErrorForOutput(final int y) {
		return totalErrors[y] / (double) sampleCounts[y];
	}

	public void clear() {
		Arrays.fill(totalErrors, 0);
		Arrays.fill(sampleCounts, 0);
	}
}
