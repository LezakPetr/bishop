package bishop.tablebase;


/**
 * CombinatorialNumberSystem is mapping between integral numbers and combinations
 * without repetition. It is immutable.
 * @author Ing. Petr Ležák
 */
public interface ICombinatorialNumberSystem {
	/**
	 * Returns mask with selected bits set.
	 * @param combinationIndex index of combination
	 * @return mask with selected bits set
	 */
	public long getCombinationMask(final int combinationIndex);
	
	/**
	 * Returns combination index for given items.
	 * @param itemArray array of selected items
	 * @return combination index or -1 in case of wrong combination
	 */
	public int getCombinationIndex(final int[] itemArray);
	
	/**
	 * Returns number of available items.
	 * @return number of available items
	 */
	public int getN();
	
	/**
	 * Returns number of selected items.
	 * @return number of selected items
	 */
	public int getK();
	
	/**
	 * Returns number of combinations.
	 * @return number of combinations
	 */
	public int getCombinationCount();
}
