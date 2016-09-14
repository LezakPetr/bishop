package bishop.evaluationStatistics;

import bishop.base.Color;
import bishop.base.GameResult;

public class MaterialStatistics {
	
	private long totalCount;   // wins + draws + loses 
	private long diff;   // Wins - loses   (from White point of view)
	
	/**
	 * Returns balance.
	 * @return +1 means white wins everything, -1 means black wins everything, 0 means same chances
	 */
	public double getBalance() {
		return (double) diff / (double) getTotalCount();
	}

	public long getTotalCount() {
		return totalCount;
	}
	
	public void add (final MaterialStatistics statistics) {
		this.totalCount += statistics.totalCount;
		this.diff += statistics.diff;
	}
	
	public void addOpposite(final MaterialStatistics statistics) {
		this.totalCount += statistics.totalCount;
		this.diff -= statistics.diff;
	}

	@SuppressWarnings("incomplete-switch")
	public void addResult (final GameResult result) {
		switch (result) {
			case DRAW:
				totalCount++;
				break;
				
			case WHITE_WINS:
				totalCount++;
				diff++;
				break;
		
			case BLACK_WINS:
				totalCount++;
				diff--;
				break;
		}
	}

	public MaterialStatistics copy() {
		final MaterialStatistics result = new MaterialStatistics();
		result.totalCount = totalCount;
		result.diff = diff;
		
		return result;
	}
	
	public void negate() {
		diff = -diff;
	}

}
