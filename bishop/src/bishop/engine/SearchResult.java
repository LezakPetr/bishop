package bishop.engine;

import bishop.base.MoveList;
import bishop.base.PseudoLegalMoveGenerator;

public final class SearchResult implements ISearchResult {

	private final NodeEvaluation nodeEvaluation;
	private final MoveList principalVariation;
	private long nodeCount;
	private int horizon;
	private boolean searchTerminated;
	private final EvaluatedMoveList rootMoveList;   // List of evaluated moves in the root
	
	public SearchResult() {
		this.nodeEvaluation = new NodeEvaluation();
		this.principalVariation = new MoveList();
		this.nodeCount = 0;
		this.searchTerminated = false;
		this.rootMoveList = new EvaluatedMoveList(PseudoLegalMoveGenerator.MAX_MOVES_IN_POSITION);
	}
	
	public NodeEvaluation getNodeEvaluation() {
		return nodeEvaluation;
	}
	
	public MoveList getPrincipalVariation() {
		return principalVariation;
	}
	
	public long getNodeCount() {
		return nodeCount;
	}
	
	public void setNodeCount (final long nodeCount) {
		this.nodeCount = nodeCount;
	}
	
	public SearchResult copy() {
		final SearchResult result = new SearchResult();
		
		result.getNodeEvaluation().assign(this.nodeEvaluation.copy());
		result.getPrincipalVariation().assign(this.principalVariation);
		result.nodeCount = this.nodeCount;
		result.horizon = horizon;
		result.searchTerminated = searchTerminated;
		
		return result;
	}

	public void clear() {
		this.nodeEvaluation.clear();
		this.principalVariation.clear();
		this.nodeCount = 0;
	}

	public boolean isSearchTerminated() {
		return searchTerminated;
	}

	public void setSearchTerminated(final boolean searchTerminated) {
		this.searchTerminated = searchTerminated;
	}

	public int getHorizon() {
		return horizon;
	}

	public void setHorizon(int horizon) {
		this.horizon = horizon;
	}

	public EvaluatedMoveList getRootMoveList() {
		return rootMoveList;
	}

}
