package bishop.tablebase;

import bishop.tables.CombinationNumberTable;

public class TwoCombinatorialNumberSystem implements ICombinatorialNumberSystem {
	
	private static final int K = 2;
	
	private final int n;
	private final int combinationCount;
	

	/**
	 * Creates CombinatorialNumberSystem for C(n, 2).
	 * @param n number of available items
	 */
	public TwoCombinatorialNumberSystem(final int n) {
		if (n < K || n > Long.SIZE * Byte.SIZE)
			throw new RuntimeException("Invalid n: " + n);
				
		this.n = n;
		this.combinationCount = CombinationNumberTable.getItem(n, K);
	}
	
	/**
	 * Returns mask with selected bits set.
	 * @param combinationIndex index of combination
	 * @return mask with selected bits set
	 */
	public long getCombinationMask(final int combinationIndex) {
		return GeneralCombinatorialNumberSystem.getCombinationMask (n, K, combinationIndex);
	}
	
	/**
	 * Returns combination index for given items.
	 * @param itemArray array of selected items
	 * @return combination index or -1 in case of wrong combination
	 */
	public int getCombinationIndex(final int[] itemArray) {
		if (itemArray.length != K)
			throw new RuntimeException("Wrong item count");
		
		final int first = itemArray[0];
		final int second = itemArray[1];
		final int low = Math.min(first, second);
		final int high = Math.max(first, second);
		
		return high * (high - 1) / 2 + low;
	}
	
	/**
	 * Returns number of available items.
	 * @return number of available items
	 */
	public int getN() {
		return n;
	}
	
	/**
	 * Returns number of selected items.
	 * @return number of selected items
	 */
	public int getK() {
		return K;
	}

	/**
	 * Returns number of combinations.
	 * @return number of combinations
	 */
	public int getCombinationCount() {
		return combinationCount;
	}
}
