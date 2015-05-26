package bishop.base;

public class Holder<T> {

	private T value = null;

	public T getValue() {
		return value;
	}

	public void setValue(final T value) {
		this.value = value;
	}
	
}
