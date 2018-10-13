package bishop.base;

public class BitBoardCombinator {
	
	private final long mask;
	private long combination;
	private long remainingCombinations;
	
	public BitBoardCombinator(final long mask) {
		this.mask = mask;
		this.combination = 0;
		this.remainingCombinations = getCombinationCount();
	}

	public boolean hasNextCombination() {
		return remainingCombinations > 0;
	}
	
	public long getNextCombination() {
		final long combinationToReturn = combination;

		// We want then number 'combination' to be incremented by 1 in bits of mask. We can simply add 1
		// to it, but we must ensure that the overflow is carried between non-continuous bits.
		// So we first set the bits that are not in the mask to 1, then we increment it by 1
		// and then we clears the bits out of mask again. So:
		//   combination = (combination + ~mask + 1) & mask
		// Because ~mask + 1 = -mask we have:
		//   combination = (combination - mask) & mask
		// See The Art Of Computer Programming, Volume 1, Fascicle 1: Working with fragmented fields, Equation 84
		combination = (combination - mask) & mask;
		remainingCombinations--;
		
		return combinationToReturn;
	}

	public long getCombinationCount() {
		return 1L << BitBoard.getSquareCount(mask);
	}
}
