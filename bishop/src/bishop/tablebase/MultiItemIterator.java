package bishop.tablebase;

import bishop.base.BitBoard;

public abstract class MultiItemIterator {
	
	protected final int[] itemArray;
	
	protected MultiItemIterator(final int count) {
		this.itemArray = new int[count];
	}
	
	public long getItemMask() {
		long mask = 0;
		
		for (int item: itemArray) {
			mask |= BitBoard.getSquareMask(item);
		}
		
		return mask;
	}
	
	public void getItems (final int[] items) {
		System.arraycopy(itemArray, 0, items, 0, itemArray.length);
	}
	
	public int getItemAt (final int index) {
		return itemArray[index];
	}
	
	public int getItemCount() {
		return itemArray.length;
	}
	
	protected void assign (final MultiItemIterator orig) {
		System.arraycopy(orig.itemArray, 0, this.itemArray, 0, orig.itemArray.length);
	}
}
