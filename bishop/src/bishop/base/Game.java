package bishop.base;

public class Game {

	private GameHeader header;
	private GameNodeImpl rootNode;
	
	public Game() {
		header = new GameHeader();
		
		final Position position = new Position();
		position.setInitialPosition();
		
		newGame(position);
	}

	public GameHeader getHeader() {
		return header;
	}

	public ITreeIterator<IGameNode> getRootIterator() {
		return new GameNodeIterator (rootNode);
	}
	
	public void newGame (final Position startPosition) {
		rootNode = new GameNodeImpl();
		rootNode.getTargetPosition().assign(startPosition);
		rootNode.setMoveNumber(0);
	}
	
	private void debugCheckIntegrity() {
		if (GlobalSettings.isDebug())
			rootNode.checkIntegrity();
	}
	
	private GameNodeImpl createLastChild (final GameNodeImpl parentNode) {
		final GameNodeImpl lastChildNode = parentNode.getLastChild();
		
		final GameNodeImpl newChildNode = new GameNodeImpl();
		final int parentMoveNumber = parentNode.getMoveNumber();
		final int movingColor = parentNode.getTargetPosition().getOnTurn();
		
		newChildNode.setPreviousSibling(lastChildNode);
		newChildNode.setParent(parentNode);
		newChildNode.setMoveNumber((movingColor == Color.WHITE) ? parentMoveNumber + 1 : parentMoveNumber);
		
		if (lastChildNode != null)
			lastChildNode.setNextSibling(newChildNode);
		else
			parentNode.setFirstChild(newChildNode);
		
		parentNode.setLastChild(newChildNode);
		
		return newChildNode;
	}
	
	public ITreeIterator<IGameNode> addChild (final ITreeIterator<IGameNode> parentIterator, final Move move) {
		final GameNodeImpl parentNode = (GameNodeImpl) parentIterator.getItem();
		final GameNodeImpl childNode = createLastChild (parentNode);
		
		childNode.getMove().assign(move);
		
		final Position position = parentNode.getTargetPosition().copy();
		position.makeMove(move);
		
		childNode.getTargetPosition().assign(position);
		debugCheckIntegrity();
		
		return new GameNodeIterator (childNode);
	}
	
	private void removeNode (final GameNodeImpl childNode) {
		final GameNodeImpl parentNode = childNode.getParent();
		
		if (parentNode == null)
			throw new RuntimeException("Game tree is corrupted");
		
		final GameNodeImpl previousSibling = childNode.getPreviousSibling();
		final GameNodeImpl nextSibling = childNode.getNextSibling();
		
		if (previousSibling == null)
			parentNode.setFirstChild(nextSibling);
		else
			previousSibling.setNextSibling(nextSibling);

		if (nextSibling == null)
			parentNode.setLastChild(previousSibling);
		else
			nextSibling.setPreviousSibling(previousSibling);
		
		childNode.setParent(null);
		childNode.setPreviousSibling(null);
		childNode.setNextSibling(null);
	}
	
	private void addFirstChild (final GameNodeImpl parent, final GameNodeImpl child) {
		final GameNodeImpl oldFirstChild = parent.getFirstChild();
		
		if (oldFirstChild == null)
			parent.setLastChild(child);
		else
			oldFirstChild.setPreviousSibling(child);
		
		child.setNextSibling(oldFirstChild);
		child.setPreviousSibling(null);
		child.setParent(parent);
		parent.setFirstChild(child);
	}
	
	/**
	 * Removes given node from the game.
	 * Given iterator will be invalidated.
	 * @param iterator node iterator
	 */
	public void removeNode (final ITreeIterator<IGameNode> iterator) {
		final ITreeIterator<IGameNode> rootIterator = getRootIterator();
		
		if (iterator.equals(rootIterator))
			throw new RuntimeException("Cannot remove root node");
		
		final GameNodeImpl childNode = (GameNodeImpl) iterator.getItem();
		removeNode (childNode);
		
		debugCheckIntegrity();
	}

	/**
	 * Makes given child node first child of it's parent (ie. main line).
	 * @param iterator child iterator
	 */
	public void promoteChild (final ITreeIterator<IGameNode> iterator) {
		final GameNodeImpl child = (GameNodeImpl) iterator.getItem();
		final GameNodeImpl parent = child.getParent();
		
		if (parent != null && child.getPreviousSibling() != null) {
			removeNode(child);
			addFirstChild(parent, child);
			
			debugCheckIntegrity();
		}
	}
	
}
