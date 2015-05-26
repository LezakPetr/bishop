package bishop.base;

class GameNodeImpl implements IGameNode {

	private final Move move;
	private final Position targetPosition;
	private int moveNumber;
	private Annotation annotation;
	private String commentary;
	private GameNodeImpl parent;
	private GameNodeImpl previousSibling;
	private GameNodeImpl nextSibling;
	private GameNodeImpl firstChild;
	private GameNodeImpl lastChild;
	
	
	public GameNodeImpl() {
		move = new Move();
		targetPosition = new Position();
		setAnnotation(Annotation.NONE);
	}
	
	/**
	 * Returns move of this node.
	 * @return move of this node
	 */
	public Move getMove() {
		return move;
	}
	
	/**
	 * Returns target position of the node.
	 * @return position after the move
	 */
	public Position getTargetPosition() {
		return targetPosition;
	}
	
	/**
	 * Returns number of this move.
	 * @return move number
	 */
	public int getMoveNumber() {
		return moveNumber;
	}
	
	/**
	 * Sets number of this move.
	 * @param moveNumber move number
	 */
	public void setMoveNumber(final int moveNumber) {
		this.moveNumber = moveNumber;
	}
	
	/**
	 * Returns parent node.
	 * @return parent node
	 */
	public GameNodeImpl getParent() {
		return parent;
	}

	public void setParent(final GameNodeImpl parent) {
		this.parent = parent;
	}

	/**
	 * Returns previous sibling.
	 * @return previous sibling
	 */
	public GameNodeImpl getPreviousSibling() {
		return previousSibling;
	}

	public void setPreviousSibling(final GameNodeImpl previousSibling) {
		this.previousSibling = previousSibling;
	}

	/**
	 * Returns next sibling.
	 * @return next sibling
	 */
	public GameNodeImpl getNextSibling() {
		return nextSibling;
	}

	public void setNextSibling(final GameNodeImpl nextSibling) {
		this.nextSibling = nextSibling;
	}

	/**
	 * Returns first child.
	 * @return first child
	 */
	public GameNodeImpl getFirstChild() {
		return firstChild;
	}
	
	public void setFirstChild(final GameNodeImpl firstChild) {
		this.firstChild = firstChild;
	}

	/**
	 * Returns last child.
	 * @return last child
	 */
	public GameNodeImpl getLastChild() {
		return lastChild;
	}
	
	public void setLastChild(final GameNodeImpl lastChild) {
		this.lastChild = lastChild;
	}
	
	public void checkIntegrity() {
		Position tmpPosition = new Position();
		GameNodeImpl child = this.firstChild;
		
		while (child != null) {
			tmpPosition.assign(this.targetPosition);
			tmpPosition.makeMove(child.move);
			
			if (!tmpPosition.equals(child.targetPosition))
				throw new RuntimeException("Target position is corrupted");
			
			if (child.parent != this)
				throw new RuntimeException("Parent reference is corrupted");
			
			child.checkIntegrity();
			
			final GameNodeImpl nextSibling = child.nextSibling;
			
			if (nextSibling != null) {
				if (nextSibling.previousSibling != child)
					throw new RuntimeException("Sibling list is corrupted");
			}
			else
				break;
			
			child = nextSibling;
		}
		
		if (this.lastChild != child)
			throw new RuntimeException("Last child reference is corrupted");
	}

	/**
	 * Returns annotation of the move.
	 * @return annotation
	 */
	public Annotation getAnnotation() {
		return annotation;
	}
	
	/**
	 * Sets annotation of the move.
	 * @param annotation annotation
	 */
	public void setAnnotation(final Annotation annotation) {
		this.annotation = annotation;
	}
	
	/**
	 * Gets commentary of the move.
	 * @return commentary of the move
	 */
	public String getCommentary() {
		return commentary;
	}

	/**
	 * Sets commentary of the move.
	 * @param commentary commentary
	 */
	public void setCommentary(final String commentary) {
		this.commentary = commentary;
	}

}
