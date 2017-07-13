package math;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

public class SampledIntFunction implements IntUnaryOperator {

	private final int offset;
	private final int lowY;
	private final int highY;
	
	private final int[] samples;
	
	public SampledIntFunction (final IntUnaryOperator function, final int minX, final int maxX, final int lowY, final int highY) {
		this.offset = minX;
		this.lowY = lowY;
		this.highY = highY;
		this.samples = IntStream.range(minX, maxX)
				.map(function)
				.toArray();
	}
	
	public SampledIntFunction (final DoubleUnaryOperator function, final int minX, final int maxX, final double lowY, final double highY) {
		this (
			(int x) -> math.Utils.roundToInt(function.applyAsDouble(x)),
			minX, maxX,
			math.Utils.roundToInt(lowY), math.Utils.roundToInt(highY)
		);
	}
	
	public SampledIntFunction (final DoubleUnaryOperator function, final int minX, final int maxX) {
		this (
			function,
			minX, maxX,
			function.applyAsDouble(Double.NEGATIVE_INFINITY), function.applyAsDouble(Double.POSITIVE_INFINITY)
		);
	}
	
	@Override
	public int applyAsInt(final int x) {
		final int index = x - offset;
		
		if (index < 0)
			return lowY;
		
		if (index >= samples.length)
			return highY;
		
		return samples[index];
	}
	
	
}
