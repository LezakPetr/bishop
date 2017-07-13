package utils;

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

	public int getAndAdd(final int offset) {
		final int oldValue = value;
		value = oldValue + offset;
		
		return oldValue;
	}

}
