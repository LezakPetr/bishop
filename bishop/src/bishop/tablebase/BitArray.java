package bishop.tablebase;

public class BitArray {
	private final long[] array;
	private final long size;
	
	private static final int INDEX_SHIFT = 6;
	private static final int BITS_PER_ITEM = 1 << INDEX_SHIFT;
	private static final int INDEX_MASK = BITS_PER_ITEM - 1;
	
	public BitArray(final long size) {
		this.size = size;
		
		final int itemCount = (int) ((size + BITS_PER_ITEM - 1) >> INDEX_SHIFT);
		array = new long[itemCount];
	}
	
	public boolean getAt (final long index) {
		final int itemIndex = (int) (index >> INDEX_SHIFT);
		final long mask = 1L << (index & INDEX_MASK);
		
		return (array[itemIndex] & mask) != 0;
	}
	
	public void setAt (final long index, final boolean value) {
		final int itemIndex = (int) (index >> INDEX_SHIFT);
		final long mask = 1L << (index & INDEX_MASK);
		
		long item = array[itemIndex];
		
		if (value)
			item |= mask;
		else
			item &= ~mask;
		
		array[itemIndex] = item;
	}

	public void assignOr(final BitArray orig) {
		if (orig.size != this.size || orig.array.length != this.array.length)
			throw new RuntimeException("Different sizes of arrays");
		
		for (int i = 0; i < orig.array.length; i++) {
			this.array[i] |= orig.array[i];
		}
	}
	
	public long getSize() {
		return size;
	}

}
