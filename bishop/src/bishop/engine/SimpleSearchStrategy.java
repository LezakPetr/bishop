package bishop.engine;

public final class SimpleSearchStrategy implements ISearchStrategy {
	
	private ISearchStrategyHandler handler;
	
	
	/**
	 * Sets handler to the strategy.
	 * @param handler handler
	 */
	public void setHandler (final ISearchStrategyHandler handler) {
		this.handler = handler;
	}
	
	private void setAllTasks() {
		final SearchNode rootNode = handler.getRootNode();
		
		for (SearchNode node: rootNode.getChildren()) {
			if (handler.getWaitingEngineCount() == 0)
				break;
			
			if (node.getEvaluationState() == SearchNode.EvaluationState.NOT_EVALUATED) {
				final SearchNode parentNode = node.getParent();
				final NodeEvaluation parentEvaluation = parentNode.getEvaluation();
				
				handler.calculateNode(node, -parentEvaluation.getBeta(), -parentEvaluation.getAlpha(), false);
			}
		}
	}
	
	/**
	 * Method is called when new search is initiated.
	 */
	public void onNewSearch() {
	}
	
	/**
	 * This method is called when new iteration is started.
	 * @param horizon depth of the search
	 */
	public void onNewIteration (final int horizon) {
		final SearchNode rootNode = handler.getRootNode();
		handler.openNode(rootNode);
		
		setAllTasks();
	}
	
	/**
	 * This method is called when node is calculated.
	 * @param node calculated node
	 */
	public void onNodeCalculated (final SearchNode node) {
		setAllTasks();
	}
}
