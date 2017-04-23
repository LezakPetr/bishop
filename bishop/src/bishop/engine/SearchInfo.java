package bishop.engine;

import java.util.ArrayList;
import java.util.List;

import bishop.base.MoveList;
import bishop.base.Position;

public final class SearchInfo {

	private Position position;
	private MoveList principalVariation;   // Best found variation
	private int evaluation;
	private int horizon;   // Horizon of the search
	private long nodeCount;   // Number of searched nodes
	private long elapsedTime;   // [ms]
	private final List<String> additionalInfo = new ArrayList<>();   // Lines with additional information
	
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

	public Position getPosition() {
		return position;
	}

	public void setPosition(final Position position) {
		this.position = position;
	}

	public List<String> getAdditionalInfo() {
		return additionalInfo;
	}
	
}
