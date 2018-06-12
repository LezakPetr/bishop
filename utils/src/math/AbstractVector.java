package math;

abstract public class AbstractVector implements IVector {
	private boolean frozen;
	
	@Override
	public IVectorRead freeze() {
		this.frozen = true;
		
		return this;
	}

	protected void checkNotFrozen() {
		if (frozen)
			throw new RuntimeException("Vector is frozen");
	}
	
	@Override
	public boolean equals (final Object obj) {
		if (!(obj instanceof IVectorRead))
			return false;
		
		final IVectorRead that = (IVectorRead) obj;
		
		if (this.getDimension() != that.getDimension())
			return false;
		
		return BinaryVectorAlgorithmOneNonzero.getInstance()
				.processElements(this, that, (x, y) -> x - y, new ZeroVectorTester())
				.isZero();
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		
		for (IVectorIterator it = getNonZeroElementIterator(); it.isValid(); it.next()) {
			final double element = it.getElement();
			
			if (element != 0)
				hash ^= it.getIndex() + Double.hashCode(element);
		}
		
		return hash;
	}

	@Override
	public boolean isImmutable() {
		return frozen;
	}
	
}
