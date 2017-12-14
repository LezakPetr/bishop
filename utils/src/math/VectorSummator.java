package math;

public class VectorSummator implements IVectorElementProcessor {
	
	private double sum;
	
	@Override
	public void init(final Density density, final int dimension) {
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
