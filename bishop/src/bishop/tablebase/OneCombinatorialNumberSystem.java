package bishop.tablebase;

import bishop.base.BitBoard;

public class OneCombinatorialNumberSystem implements ICombinatorialNumberSystem {
	
	private final int n;
	
	/**
	 * Creates CombinatorialNumberSystem for C(n, 1).
	 * @param n number of available items
	 */
	public OneCombinatorialNumberSystem (final int n) {
		if (n < 1 || n > Long.SIZE * Byte.SIZE)
			throw new RuntimeException("Invalid n: " + n);

		this.n = n;
	}

	@Override
	public long getCombinationMask(final int combinationIndex) {
		return BitBoard.getSquareMask(combinationIndex);
	}

	@Override
	public int getCombinationIndex(final int[] itemArray) {
		if (itemArray.length != 1)
			throw new RuntimeException("Wrong item count");

		return itemArray[0];
	}

	@Override
	public int getN() {
		return n;
	}

	@Override
	public int getK() {
		return 1;
	}

	@Override
	public int getCombinationCount() {
		return n;
	}

}
