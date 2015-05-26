package bishop.engine;

import bishop.base.MoveList;

public final class SearchInfo {

	private MoveList principalVariation;   // Best found variation
	private int evaluation;
	private int horizon;   // Horizon of the search
	private long nodeCount;   // Number of searched nodes
	private long elapsedTime;   // [ms]
	
	public MoveList getPrincipalVariation() {
		return principalVariation;
	}
	
	public void setPrincipalVariation(final MoveList principalVariation) {
		this.principalVariation = principalVariation;
	}
	
	public int getHorizon() {
		return horizon;
	}
	
	public void setHorizon(final int horizon) {
		this.horizon = horizon;
	}
	
	public long getNodeCount() {
		return nodeCount;
	}
	
	public void setNodeCount(final long nodeCount) {
		this.nodeCount = nodeCount;
	}
	
	public long getElapsedTime() {
		return elapsedTime;
	}
	
	public void setElapsedTime(final long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public int getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(final int evaluation) {
		this.evaluation = evaluation;
	}
	
}
