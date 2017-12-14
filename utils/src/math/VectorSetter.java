package math;

/**
 * Element processor that stores the elements into the vector.
 * 
 * @author Ing. Petr Ležák
 */
public class VectorSetter implements IVectorElementProcessor {
	
	private IVector vector;
	
	@Override
	public void init(final Density density, final int dimension) {
		vector = Vectors.vectorWithDensity(density, dimension);
	}

	@Override
	public void processElement(final int index, final double value) {
		vector.setElement(index, value);
	}
	
	public IVectorRead getVector() {
		return vector.freeze();
	}

	public IVector getMutableVector() {
		return vector;
	}

}
