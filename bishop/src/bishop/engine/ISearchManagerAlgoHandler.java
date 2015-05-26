package bishop.engine;

public interface ISearchManagerAlgoHandler {
	/**
	 * Updates alpha-beta boundaries of engines that calculates direct children
	 * of given node as a result of node evaluation change.
	 * @param node node which evaluation was changed
	 */
	public void updateEnginesTaskBoundaries(final SearchNode node);
	
	/**
	 * Stops engines that calculates direct or indirect children of given node.
	 * @param parentNode parent node
	 */
	public void stopChildEngines (final SearchNode parentNode);
	
	/**
	 * Check if calculation is finished.
	 * @return true if searchFinished flag is set
	 */
	public boolean isSearchFinished();
	
	/**
	 * Checks if search should finish and if so sets searchFinished flag.
	 */
	public void updateSearchFinished();
}
