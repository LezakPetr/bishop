package bishop.engine;

public interface ISearchStrategy {
	/**
	 * Sets handler to the strategy.
	 * @param handler handler
	 */
	public void setHandler (final ISearchStrategyHandler handler);
	
	/**
	 * Method is called when new search is initiated.
	 */
	public void onNewSearch();
	
	/**
	 * This method is called when new iteration is started.
	 * @param horizon depth of the search
	 */
	public void onNewIteration (final int horizon);
	
	/**
	 * This method is called when node is calculated.
	 * This note don't have to be evaluated because in case of smaller alpha-beta window was set.
	 * @param node calculated node
	 */
	public void onNodeCalculated (final SearchNode node);
}
