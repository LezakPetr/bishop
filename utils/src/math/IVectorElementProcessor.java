package math;

public interface IVectorElementProcessor {

	public void init(final Density density, final int dimension, final int expectedNonZeroElementCount);

	public default void init(final Density density, final int dimension) {
		init (density, dimension, -1);
	}

	public void processElement (final int index, final double value);

}
