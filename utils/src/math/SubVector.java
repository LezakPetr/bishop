package math;


public class SubVector implements IVector {
	
	private final IVector baseVector;
	private final int offset;
	private final int dimension;
	
	public SubVector (final IVector baseVector, final int offset, final int dimension) {
		this.baseVector = baseVector;
		this.offset = offset;
		this.dimension = dimension;
	}

	@Override
	public int getDimension() {
		return dimension;
	}

	@Override
	public double getElement(final int index) {
		return baseVector.getElement(getSubIndex(index));
	}

	@Override
	public IVector setElement(final int index, final double value) {
		return baseVector.setElement(getSubIndex(index), value);
	}
	
	private int getSubIndex(final int index) {
		if (index < 0 || index >= dimension)
			throw new IndexOutOfBoundsException("Index out of bound " + index);
		
		return index + offset;
	}

	@Override
	public IVectorRead freeze() {
		throw new RuntimeException("Cannot freeze sub vector");
	}

	@Override
	public Density density() {
		return baseVector.density();
	}

	@Override
	public IVectorIterator getNonZeroElementIterator() {
		return new SubVectorIterator(baseVector.getNonZeroElementIterator(), offset, offset + dimension);
	}
}
