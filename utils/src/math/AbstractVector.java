package math;

abstract public class AbstractVector extends AbstractVectorRead implements IVector {
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
	public boolean isImmutable() {
		return frozen;
	}
	
}
