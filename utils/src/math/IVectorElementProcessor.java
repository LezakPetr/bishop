package math;

public interface IVectorElementProcessor {

	public void init(final Density density, final int dimension);

	public void processElement (final int index, final double value);

}
