package bishop.tablebase;

import utils.ICopyable;

/**
 * Iterator that iterates thru all combinations without repetition.
 * @author Ing. Petr Ležák
 */
public class CombinationIterator extends MultiItemIterator implements ICopyable<CombinationIterator> {
	
	private final int first;
	private final int last;
	
	/**
	 * Creates iterator.
	 * @param count number of items (size of selection)
	 * @param first first item
	 * @param last item after last item
	 */
	public CombinationIterator(final int count, final int first, final int last) {
		super (count);

		if (first >= last)
			throw new RuntimeException("Invalid item interval: <" + first + ", " + last + ")");
		
		if (count < 1 || count > (last - first))
			throw new RuntimeException("Invalid count: " + count);

		this.first = first;
		this.last = last;
		
		init();
	}
	
	/**
	 * Moves iterator to first combination.
	 */
	public void init() {
		for (int i = 0; i < itemArray.length; i++)
			this.itemArray[i] = first + i;		
	}
	
	/**
	 * Moves iterator to next combination.
	 * @return true if iterator is valid, false if not
	 */
	public boolean next() {
		int i = 0;
		
		while (true) {
			itemArray[i]++;
			
			if (i + 1 < itemArray.length) {
				if (itemArray[i] < itemArray[i + 1])
					return true;
			}
			else
				return itemArray[i] < last;
			
			itemArray[i] = first + i;
			i++;
		}
	}
	
	/**
	 * Creates copy of this iterator.
	 */
	public CombinationIterator copy() {
		final CombinationIterator copyObj = new CombinationIterator(getItemCount(), first, last);
		copyObj.assign(this);
		
		return copyObj;
	}
}
