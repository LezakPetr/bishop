package bishop.base;

public interface Copyable<T extends Copyable<T>> {
	/**
	 * Returns copy of the object.
	 * @return copy
	 */
	public T copy();
}
