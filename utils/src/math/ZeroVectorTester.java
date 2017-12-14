package math;

/**
 * Element processor that checks if the elements of the vector are zero.
 * 
 * @author Ing. Petr Ležák
 */
public class ZeroVectorTester implements IVectorElementProcessor {

	private boolean zero;
	
	@Override
	public void init(final Density density, final int dimension) {
		zero = true;
	}

	@Override
	public void processElement(final int index, final double value) {
		zero &= (value == 0);
	}
	
	public boolean isZero() {
		return zero;
	}

}
