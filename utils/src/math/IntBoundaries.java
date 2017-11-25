package math;

public class IntBoundaries {
	private final int min;
	private final int max;
	
	public IntBoundaries(final int min, final int max) {
		this.min = min;
		this.max = max;
	}
	
	public int trim (final int value) {
		if (value <= min)
			return min;
		
		if (value >= max)
			return max;
		
		return value;
	}
}
