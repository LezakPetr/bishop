package math;

public class SubVectorIterator implements IVectorIterator {
	
	private final IVectorIterator baseIterator;
	private final int begin;
	private final int dimension;
	private int index;
	
	public SubVectorIterator (final IVectorIterator baseIterator, final int begin, final int end) {
		this.baseIterator = baseIterator;
		this.begin = begin;
		this.dimension = end - begin;

		while (baseIterator.isValid() && baseIterator.getIndex() < begin)
			baseIterator.next();

		recalculateIndex();
	}
	
	@Override
	public void next() {
		if (isValid()) {
			baseIterator.next();

			recalculateIndex();
		}
	}

	private void recalculateIndex() {
		if (baseIterator.isValid())
			index = baseIterator.getIndex() - begin;
		else
			index = Integer.MAX_VALUE;
	}

	@Override
	public boolean isValid() {
		return index < dimension;
	}
	
	@Override
	public int getIndex() {
		return index;
	}
	
	@Override
	public double getElement() {
		return baseIterator.getElement();
	}

}
