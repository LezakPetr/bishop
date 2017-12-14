package math;

public class SubVectorIterator implements IVectorIterator {
	
	private final IVectorIterator baseIterator;
	private final int begin;
	private final int end;
	
	public SubVectorIterator (final IVectorIterator baseIterator, final int begin, final int end) {
		this.baseIterator = baseIterator;
		this.begin = begin;
		this.end = end;
		
		if (baseIterator.isValid() && !isInRange())
			next();
	}
	
	@Override
	public void next() {
		if (baseIterator.isValid()) {
			do {
				baseIterator.next();
			} while (baseIterator.isValid() && !isInRange());
		}
	}
	
	private boolean isInRange() {
		final int index = baseIterator.getIndex();
		
		return index >= begin && index < end;
	}
	
	@Override
	public boolean isValid() {
		return baseIterator.isValid();
	}
	
	@Override
	public int getIndex() {
		return baseIterator.getIndex() - begin;
	}
	
	@Override
	public double getElement() {
		return baseIterator.getElement();
	}

}
