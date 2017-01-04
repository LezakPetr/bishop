package bishop.bookBuilder;

import java.util.Arrays;
import bishop.base.Color;
import bishop.base.GameResult;
import bishop.base.Move;
import bishop.base.MoveList;

public class PositionStatistics {
	
	private static final int[] EMPTY_COUNT_ARRAY = new int[0];
	
	private final MoveList moves = new MoveList();
	private int[] counts = EMPTY_COUNT_ARRAY;
	private int totalCount;   // wins + draws + loses 
	private int diff;   // Wins - loses
	
	
	public void addMove (final int onTurn, final Move move, final GameResult result) {
		final int index = moves.indexOf (move);
		
		if (index >= 0)
			counts[index] ++;
		else {
			final int size = moves.getSize();
			
			moves.add(move);
			counts = Arrays.copyOf(counts, size + 1);
			
			counts[size] = 1;
		}
		
		addStatistics(onTurn, result);
	}
	
	public Iterable<Move> getMoves() {
		return moves;
	}
	
	public int getMoveCount (final Move move) {
		final int index = moves.indexOf (move);
		
		return counts[index];
	}
		
	@SuppressWarnings("incomplete-switch")
	private void addStatistics (final int onTurn, final GameResult result) {
		switch (result) {
			case DRAW:
				totalCount++;
				break;
				
			case WHITE_WINS:
				totalCount++;
				
				if (onTurn == Color.WHITE)
					diff++;
				else
					diff--;
				break;
		
			case BLACK_WINS:
				totalCount++;
				
				if (onTurn == Color.BLACK)
					diff++;
				else
					diff--;
				break;
		}
	}

	public double getBalance() {
		return (double) diff / (double) getTotalCount();
	}

	public long getTotalCount() {
		return totalCount & 0xFFFFFFFFL;
	}

}
