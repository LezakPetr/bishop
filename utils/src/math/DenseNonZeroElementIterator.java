package math;

public class DenseNonZeroElementIterator implements IVectorIterator {

	private final IVectorRead vector;
	private int index = -1;
	
	public DenseNonZeroElementIterator(final IVectorRead vector) {
		this.vector = vector;
		
		next();
	}

	@Override
	public boolean isValid() {
		return index < vector.getDimension();
	}

	@Override
	public void next() {
		do {
			index++;
		} while (index < vector.getDimension() && vector.getElement(index) == 0);
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public double getElement() {
		return vector.getElement(index);
	}

}
