package bishop.engine;

import java.util.List;

import bishop.engine.SearchNode.EvaluationState;
import bishop.engine.SearchNode.TreeState;

/**
 * Principal variation splitting strategy evaluates principal move first
 * before other moves are searched concurrently. The principal move is again searched
 * with same rule. This means that search tree looks like this:
 * -+- e4 -+- e5 -+- Jf3 -+- Jc6  - Principal variation 
 *  +- e3  +- e6  +- d4   +- d5   \
 *  +- d4  +- d5  +- d3   +- d6   | Parallel calculation
 *  +- d2  +- d6                  /
 *  
 * @author Ing. Petr Ležák
 */
public final class PrincipalVariationSplittingStrategy implements ISearchStrategy {
	
	private ISearchStrategyHandler handler;
	
	// Number of moves before horizon when to stop splitting
	private static final int DEPTH_TRESHOLD = 3;
	
	
	/**
	 * Sets handler to the strategy.
	 * @param handler handler
	 */
	public void setHandler (final ISearchStrategyHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * Method is called when new search is initiated.
	 */
	public void onNewSearch() {
	}
	
	/**
	 * Finds opened (not leaf) unevaluated search node with maximal depth.
	 * @return node
	 */
	private SearchNode getDeepestUnevaluatedNode() {
		SearchNode node = handler.getRootNode();
		
		while (node.getEvaluationState() == EvaluationState.NOT_EVALUATED && node.getTreeState() == TreeState.OPENED)
			node = node.getChildren().get(0);
		
		return node.getParent();
	}
	
	/**
	 * Takes deepest unevaluated node and makes as many tasks from its children as possible,
	 */
	private void setAllTasks() {
		final SearchNode actualNode = getDeepestUnevaluatedNode();
		
		if (actualNode != null) {
			boolean isMoveEvaluated = false;
			
			for (SearchNode child: actualNode.getChildren()) {
				if (handler.getWaitingEngineCount() == 0)
					return;
				
				final EvaluationState evaluationState = child.getEvaluationState();
				final TreeState treeState = child.getTreeState();
				
				if (evaluationState == EvaluationState.NOT_EVALUATED && treeState != TreeState.OPENED) {
					final SearchNode parentNode = child.getParent();
					final NodeEvaluation parentEvaluation = parentNode.getEvaluation();
					
					final int childAlpha = -parentEvaluation.getBeta();
					final int childBeta = -parentEvaluation.getAlpha();
					
					// Principal variation search
					if (!isMoveEvaluated || childBeta - childAlpha == 1)
						calculateNodeWithFullWindow(child);
					else {
						handler.calculateNode(child, childBeta - 1, childBeta, true);						
					}
				}
				
				if (evaluationState == EvaluationState.EVALUATED)
					isMoveEvaluated = true;
			}
		}
	}

	private void calculateNodeWithFullWindow(final SearchNode node) {
		final SearchNode parentNode = node.getParent();
		final NodeEvaluation parentEvaluation = parentNode.getEvaluation();
		
		handler.calculateNode(node, -parentEvaluation.getBeta(), -parentEvaluation.getAlpha(), false);
	}
	
	/**
	 * This method is called when new iteration is started.
	 * @param horizon depth of the search
	 */
	public void onNewIteration (final int horizon) {
		SearchNode node = handler.getRootNode();
		
		// Open the tree up to DEPTH_TRESHOLD before horizon.
		while (node.getDepth() == 0 || node.getDepth() < horizon / ISearchEngine.HORIZON_GRANULARITY - DEPTH_TRESHOLD) {
			handler.openNode(node);
			
			final List<SearchNode> children = node.getChildren();
			
			if (children.isEmpty())
				break;
			
			node = children.get(0);
		}
		
		setAllTasks();
	}
	
	/**
	 * This method is called when node is calculated.
	 * This note don't have to be evaluated because of closed alpha-beta window.
	 * @param node calculated node
	 */
	public void onNodeCalculated (final SearchNode node) {
		if (node.getEvaluationState() == EvaluationState.EVALUATED)
			setAllTasks();
		else {
			// Due to wrong order of moves in principal variation search node was not evaluated.
			// Evaluate it again with full window.
			calculateNodeWithFullWindow(node);
		}
	}

}
