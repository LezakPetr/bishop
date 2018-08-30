package math;

/**
 * Element processor that calculates sum of the elements.
 * 
 * @author Ing. Petr Ležák
 */
public class VectorElementSum implements IVectorElementProcessor {
	
	private double sum;
	
	@Override
	public void init(final Density density, final int dimension, final int expectedNonZeroElementCount) {
		sum = 0.0;
	}

	@Override
	public void processElement(final int index, final double value) {
		sum += value;
	}

	public double getSum() {
		return sum;
	}
}
