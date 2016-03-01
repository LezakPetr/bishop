package bishop.base;

public class Holder<T> {

	private T value;
	
	public Holder() {
		this.value = null;
	}
	
	public Holder(final T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(final T value) {
		this.value = value;
	}
	
}
