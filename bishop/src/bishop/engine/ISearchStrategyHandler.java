package bishop.engine;

public interface ISearchStrategyHandler {
	/**
	 * Returns number of waiting search engines.
	 * @return number of search engines that are waiting for some task
	 */
	public int getWaitingEngineCount();
	
	/**
	 * Returns total number of search engines.
	 * @return total number of search engines
	 */
	public int getTotalEngineCount();
	
	/**
	 * Adds task to some waiting search engine.
	 * Throws an exception if there is no waiting engine.
	 * @param node node to calculate
	 */
	public void calculateNode (final SearchNode node, final int alpha, final int beta, final boolean isSmallerWindow);
	
	/**
	 * Returns root node of the search.
	 * @return root node of the search
	 */
	public SearchNode getRootNode();
	
	/**
	 * Opens given node.
	 * @param node  node to open
	 */
	public void openNode (final SearchNode node);
}
