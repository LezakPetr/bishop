package bishop.base;

public interface IGameNode {
	/**
	 * Returns move of this node.
	 * @return move of this node
	 */
	public Move getMove();
	
	/**
	 * Returns target position of the node.
	 * @return position after the move
	 */
	public Position getTargetPosition();
	
	/**
	 * Returns number of this move.
	 * @return move number
	 */
	public int getMoveNumber();
	
	/**
	 * Returns annotation of the move.
	 * @return annotation
	 */
	public Annotation getAnnotation();
	
	/**
	 * Sets annotation of the move.
	 * @param annotation annotation
	 */
	public void setAnnotation(final Annotation annotation);
	
	/**
	 * Gets commentary of the move.
	 * @return commentary of the move
	 */
	public String getCommentary();

	/**
	 * Sets commentary of the move.
	 * @param commentary commentary
	 */
	public void setCommentary(final String commentary);
}
