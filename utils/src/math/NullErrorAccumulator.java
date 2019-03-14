package math;

public class NullErrorAccumulator implements IErrorAccumulator {

	private static final NullErrorAccumulator INSTANCE = new NullErrorAccumulator();

	private NullErrorAccumulator() {
	}

	@Override
	public void addSample(final double predictedY, final int expectedY) {
	}

	@Override
	public void clear() {
	}

	public static NullErrorAccumulator getInstance() {
		return INSTANCE;
	}
}
