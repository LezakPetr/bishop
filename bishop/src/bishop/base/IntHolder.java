package bishop.base;

public class IntHolder {
	private int value;
	
	public IntHolder() {
	}
	
	public IntHolder(final int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(final int value) {
		this.value = value;
	}
	
	public int next() {
		return next(1);
	}
	
	public int next(final int count) {
		final int old = value;
		value += count;
		
		return old;
	}

}
