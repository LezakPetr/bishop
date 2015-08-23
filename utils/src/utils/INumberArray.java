package utils;

public interface INumberArray {
	/**
	 * Returns maximal possible stored element (exclusive).
	 * @return maximal possible stored element
	 */
	public int getMaxElement();
	
	/**
	 * Returns number of elements
	 * @return size
	 */
	public long getSize();
	
	/**
	 * Returns element at given index.
	 * @param index index of element
	 * @return value
	 */
	public int getAt (final long index);
	
	/**
	 * Sets element at given index
	 * @param index index
	 * @param element new value
	 */
	public void setAt (final long index, final int element);
}
