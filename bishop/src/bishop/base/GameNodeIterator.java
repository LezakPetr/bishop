package bishop.base;

public class GameNodeIterator implements ITreeIterator<IGameNode> {
	
	private GameNodeImpl node;
	
	
	/**
	 * Creates iterator above given node.
	 * @param node game node
	 */
	public GameNodeIterator (final GameNodeImpl node) {
		this.node = node;
	}

	/**
	 * Returns item pointed by this iterator.
	 * @return item
	 */
	public GameNodeImpl getItem() {
		return node;
	}
	
	/**
	 * Checks if there is previous sibling.
	 * @return true if there is previous sibling, false if not
	 */
	public boolean hasPreviousSibling() {
		return node.getPreviousSibling() != null;
	}
	
	/**
	 * Moves this iterator to previous sibling.
	 */
	public void movePreviousSibling() {
		node = node.getPreviousSibling();
	}

	/**
	 * Checks if there is next sibling.
	 * @return true if there is next sibling, false if not
	 */
	public boolean hasNextSibling() {
		return node.getNextSibling() != null;
	}

	/**
	 * Moves this iterator to next sibling.
	 */
	public void moveNextSibling() {
		node = node.getNextSibling();
	}
	
	/**
	 * Checks if there is parent node.
	 * @return true if there is parent node, false if not
	 */
	public boolean hasParent() {
		return node.getParent() != null;
	}

	/**
	 * Moves this iterator to parent node.
	 */
	public void moveParent() {
		node = node.getParent();
	}

	/**
	 * Checks if there is some child.
	 * @return true if there is some child, false if not
	 */
	public boolean hasChild() {
		return node.getFirstChild() != null;
	}

	/**
	 * Moves this iterator to the first child.
	 */
	public void moveFirstChild() {
		node = node.getFirstChild();
	}
	
	/**
	 * Returns new iterator pointing to same node.
	 * @return copy of current iterator
	 */
	public GameNodeIterator copy() {
		return new GameNodeIterator (node);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof GameNodeIterator))
			return false;
		
		return node == ((GameNodeIterator) obj).node;
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(node);
	}

}
