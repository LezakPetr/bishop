package math;

/**
 * Element processor that checks if the elements of the vector are zero.
 * 
 * @author Ing. Petr Ležák
 */
public class ZeroVectorTester implements IVectorElementProcessor {

	private double firstNonZeroValue = 0.0;
	
	@Override
	public void init(final Density density, final int dimension, final int expectedNonZeroElementCount) {
		firstNonZeroValue = 0.0;
	}

	@Override
	public void processElement(final int index, final double value) {
		if (firstNonZeroValue == 0.0)
			firstNonZeroValue = value;
	}
	
	public boolean isZero() {
		return firstNonZeroValue == 0;
	}

	public double getFirstNonZeroValue() {
		return firstNonZeroValue;
	}

}
