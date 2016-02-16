package bishop.tablebase;

import java.util.Arrays;

import bishop.base.BitBoard;
import bishop.tables.CombinationNumberTable;

public final class GeneralCombinatorialNumberSystem implements ICombinatorialNumberSystem {
	
	private final int n;
	private final int k;
	private final int combinationCount;
	

	/**
	 * Creates CombinatorialNumberSystem for C(n, k).
	 * @param n number of available items
	 * @param k number of selected items
	 */
	public GeneralCombinatorialNumberSystem(final int n, final int k) {
		if (n < 1 || n > Long.SIZE * Byte.SIZE)
			throw new RuntimeException("Invalid n: " + n);
		
		if (k < 1 || k > n)
			throw new RuntimeException("Invalid k: " + k);
		
		this.n = n;
		this.k = k;
		this.combinationCount = CombinationNumberTable.getItem(n, k);
	}
	
	/**
	 * Returns mask with selected bits set.
	 * @param combinationIndex index of combination
	 * @return mask with selected bits set
	 */
	public long getCombinationMask(final int combinationIndex) {
		return getCombinationMask (n, k, combinationIndex);
	}
	
	public static long getCombinationMask (final int n, final int k, final int combinationIndex) {
		int index = combinationIndex;
		int maxItem = n;
		long mask = 0;
		
		for (int i = k; i > 0; i--) {
			final int item = Utils.findCombinationNumber(maxItem, i, index);
			maxItem = item;
			
			index -= CombinationNumberTable.getItem(item, i);
			mask |= BitBoard.getSquareMask(item);
		}
		
		return mask;		
	}
	
	/**
	 * Returns combination index for given items.
	 * @param itemArray array of selected items
	 * @return combination index or -1 in case of wrong combination
	 */
	public int getCombinationIndex(final int[] itemArray) {
		if (itemArray.length != k)
			throw new RuntimeException("Wrong item count");
		
		final int[] sortedArray = new int[k];
		System.arraycopy(itemArray, 0, sortedArray, 0, k);
		Arrays.sort(sortedArray);
		
		int index = 0;
		
		for (int i = k; i > 0; i--) {
			index += CombinationNumberTable.getItem(sortedArray[i-1], i);
		}
		
		return index;
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
		return k;
	}

	/**
	 * Returns number of combinations.
	 * @return number of combinations
	 */
	public int getCombinationCount() {
		return combinationCount;
	}

}
