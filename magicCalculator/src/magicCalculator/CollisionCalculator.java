package magicCalculator;


import java.util.Arrays;

import optimization.IEvaluator;
import bishop.base.BitBoard;
import bishop.base.BitBoardCombinator;
import bishop.base.Square;

public class CollisionCalculator implements IEvaluator<MagicState, MagicSettings> {
	
	private long mask;
	private long[] combinations;
	
	private boolean[] occupiedCells;
	private int collisionCount;
	
	private MagicSettings settings;
	
	@Override
	public double evaluateState(final MagicState state) {
		calculate(state);
		
		return collisionCount;
	}

	private void calculate(final MagicState state) {
		initializeCombinations();
		initializeOccupiedCells();
		calculateCollisions(state.getCoeff());
	}
	
	public boolean hasCollisions(final MagicState state) {
		calculate(state);
		
		return collisionCount > 0;
	}
	
	private void calculateCollisions(final long coeff) {
		final int depth = settings.getDepth();
		
		Arrays.fill(occupiedCells, false);
		collisionCount = 0;
		
		for (long occupancy: combinations) {
			final long product = occupancy * coeff;
			final int cellIndex = (int) (product >>> (Square.LAST - depth));
		
			if (occupiedCells[cellIndex])
				collisionCount++;
			else
				occupiedCells[cellIndex] = true;
		}
		
	}
	
	@Override
	public void setSettings(final MagicSettings settings) {
		this.settings = settings;
	}
	
	private void initializeCombinations() {
		if (settings.getMask() == mask)
			return;
		
		mask = settings.getMask();
		
		final BitBoardCombinator combinator = new BitBoardCombinator(mask);
		final int combinationCount = (int) combinator.getCombinationCount();
		combinations = new long[combinationCount];
		
		int combinationIndex = 0;
		
		while (combinator.hasNextCombination()) {
			combinations[combinationIndex] = combinator.getNextCombination();
			combinationIndex++;
		}
	}
	
	private void initializeOccupiedCells() {
		final int depth = settings.getDepth();
		final int size = 1 << depth;
		
		if (occupiedCells == null || occupiedCells.length != size)
			occupiedCells = new boolean[size];
	}

}
