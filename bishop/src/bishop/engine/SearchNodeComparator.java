package bishop.engine;

import java.util.Comparator;

import bishop.engine.SearchNode.EvaluationState;

/**
 * Comparator that compares two search nodes.
 * First criteria is evaluation state - evaluated nodes are always lower than not evaluated.
 * Second criteria is evaluation.
 */
public class SearchNodeComparator implements Comparator<SearchNode> {
	public int compare(final SearchNode node1, final SearchNode node2) {
		final EvaluationState evaluationState1 = node1.getEvaluationState();
		final EvaluationState evaluationState2 = node2.getEvaluationState();
		
		if (evaluationState1 == EvaluationState.EVALUATED && evaluationState2 == EvaluationState.NOT_EVALUATED)
			return -1;
		
		if (evaluationState1 == EvaluationState.NOT_EVALUATED && evaluationState2 == EvaluationState.EVALUATED)
			return +1;
			
		return Integer.signum(node1.getEvaluation().getEvaluation() - node2.getEvaluation().getEvaluation());
	}

	public static final SearchNodeComparator INSTANCE = new SearchNodeComparator();
	
}
