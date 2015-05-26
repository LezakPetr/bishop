package bishop.base;

public interface ITreeIterator<T> {

	/**
	 * Returns item pointed by this iterator.
	 * @return item
	 */
	public T getItem();
	
	/**
	 * Checks if there is previous sibling.
	 * @return true if there is previous sibling, false if not
	 */
	public boolean hasPreviousSibling();
	
	/**
	 * Moves this iterator to previous sibling.
	 */
	public void movePreviousSibling();

	/**
	 * Checks if there is next sibling.
	 * @return true if there is next sibling, false if not
	 */
	public boolean hasNextSibling();

	/**
	 * Moves this iterator to next sibling.
	 */
	public void moveNextSibling();
	
	/**
	 * Checks if there is parent node.
	 * @return true if there is parent node, false if not
	 */
	public boolean hasParent();

	/**
	 * Moves this iterator to parent node.
	 */
	public void moveParent();

	/**
	 * Checks if there is some child.
	 * @return true if there is some child, false if not
	 */
	public boolean hasChild();

	/**
	 * Moves this iterator to the first child.
	 */
	public void moveFirstChild();
	
	/**
	 * Returns new iterator pointing to same node.
	 * @return copy of current iterator
	 */
	public ITreeIterator<T> copy();
}
