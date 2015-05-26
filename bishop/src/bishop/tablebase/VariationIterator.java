package bishop.tablebase;

import utils.ICopyable;


/**
 * Iterator that iterates thru all variations with repetition.
 * @author Ing. Petr Ležák
 */
public class VariationIterator extends MultiItemIterator implements ICopyable<VariationIterator> {
	
	private final int[] first;
	private final int[] last;
	
	/**
	 * Creates iterator.
	 * @param count number of items (size of selection)
	 * @param first first item
	 * @param last item after last item
	 */
	public VariationIterator(final int count, final int first, final int last) {
		this (Utils.createFilledArray (first, count), Utils.createFilledArray (first, count));
	}
	
	/**
	 * Creates iterator.
	 * @param count number of items (size of selection)
	 * @param first first item
	 * @param last item after last item
	 */
	public VariationIterator(final int[] first, final int[] last) {
		super (first.length);
		
		final int count = getItemCount();
		
		if (count < 1)
			throw new RuntimeException("Invalid count: " + count);

		for (int i = 0; i < count; i++) {
			if (first[i] >= last[i])
				throw new RuntimeException("Invalid item interval: <" + first[i] + ", " + last[i] + ")");
		}

		this.first = new int[count];
		System.arraycopy(first, 0, this.first, 0, count);
		
		this.last = new int[count];
		System.arraycopy(last, 0, this.last, 0, count);
		
		init();
	}
	
	/**
	 * Moves iterator to first variation.
	 */
	public void init() {
		System.arraycopy(this.first, 0, this.itemArray, 0, getItemCount());
	}
	
	/**
	 * Moves iterator to next variation.
	 * @return true if iterator is valid, false if not
	 */
	public boolean next() {
		int i = 0;
		
		while (i < itemArray.length) {
			itemArray[i]++;
			
			if (itemArray[i] < last[i])
				break;
			
			itemArray[i] = first[i];
			i++;
		}
		
		return i < itemArray.length;
	}
	
	/**
	 * Creates copy of this iterator.
	 */
	public VariationIterator copy() {
		final VariationIterator copyObj = new VariationIterator(first, last);
		copyObj.assign(this);
		
		return copyObj;
	}

}
