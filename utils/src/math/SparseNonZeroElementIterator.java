package math;

public class SparseNonZeroElementIterator implements IVectorIterator {
	
	private final SparseVector vector;
	private int sparseIndex;
	
	public SparseNonZeroElementIterator(final SparseVector vector) {
		this.vector = vector;
	}

	@Override
	public boolean isValid() {
		return sparseIndex < vector.getNonZeroElementCount();
	}

	@Override
	public void next() {
		sparseIndex++;
	}

	@Override
	public int getIndex() {
		return vector.getNonZeroIndex(sparseIndex);
	}

	@Override
	public double getElement() {
		return vector.getNonZeroElement(sparseIndex);
	}
	
}
