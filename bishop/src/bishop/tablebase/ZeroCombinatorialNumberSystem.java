package bishop.tablebase;

import bishop.base.BitBoard;

public class ZeroCombinatorialNumberSystem implements ICombinatorialNumberSystem {
	
	private final int n;
	
	/**
	 * Creates CombinatorialNumberSystem for C(n, 0).
	 * @param n number of available items
	 */
	public ZeroCombinatorialNumberSystem (final int n) {
		this.n = n;
	}

	@Override
	public long getCombinationMask(final int combinationIndex) {
		return BitBoard.EMPTY;
	}

	@Override
	public int getCombinationIndex(final int[] itemArray) {
		if (itemArray.length != 0)
			throw new RuntimeException("Wrong item count");

		return 0;
	}

	@Override
	public int getN() {
		return n;
	}

	@Override
	public int getK() {
		return 0;
	}

	@Override
	public int getCombinationCount() {
		return 1;
	}

}
