package bishop.base;

public class BitBoardCombinator {
	
	private final int[] squares;
	private long combinationIndex;
	
	public BitBoardCombinator(final long mask) {
		final int squareCount = BitBoard.getSquareCount(mask);
		squares = new int[squareCount];
		
		fillSquaresFromMask(mask);
		
		combinationIndex = 0;
	}

	private void fillSquaresFromMask(final long mask) {
		int index = 0;
		
		for (BitLoop loop = new BitLoop(mask); loop.hasNextSquare(); ) {
			squares[index] = loop.getNextSquare();
			index++;
		}
	}
	
	public boolean hasNextCombination() {
		final long combinationCount = (1L << squares.length);
		
		return combinationIndex < combinationCount;
	}
	
	public long getNextCombination() {
		final long combination = getCombinationFromIndex();
		combinationIndex++;
		
		return combination;
	}

	private long getCombinationFromIndex() {
		long combination = 0;
		
		for (int i = 0; i < squares.length; i++) {
			final int square = squares[i];
			
			combination |= ((combinationIndex >>> i) & 0x01L) << square;
		}
		
		return combination;
	}

	public long getCombinationCount() {
		return 1L << squares.length;
	}
}
