package bishop.tablebase;

import java.util.concurrent.atomic.AtomicLongArray;

public class BitArray {
	private final AtomicLongArray array;
	private final long size;
	
	private static final int INDEX_SHIFT = 6;
	private static final int BITS_PER_ITEM = 1 << INDEX_SHIFT;
	private static final int INDEX_MASK = BITS_PER_ITEM - 1;
	
	public BitArray(final long size) {
		this.size = size;
		
		final int itemCount = (int) ((size + BITS_PER_ITEM - 1) >> INDEX_SHIFT);
		array = new AtomicLongArray(itemCount);
	}
	
	public boolean getAt (final long index) {
		final int itemIndex = (int) (index >> INDEX_SHIFT);
		final long mask = 1L << (index & INDEX_MASK);
		
		return (array.get(itemIndex) & mask) != 0;
	}
	
	public void setAt (final long index, final boolean value) {
		final int itemIndex = (int) (index >> INDEX_SHIFT);
		final long mask = 1L << (index & INDEX_MASK);
		
		if (value)
			array.updateAndGet(itemIndex, x -> x | mask);
		else
			array.updateAndGet(itemIndex, x -> x & ~mask);
	}
	
	public long getSize() {
		return size;
	}

	public void clear() {
		final int length = array.length();
		
		for (int i = length - 1; i > 0; i--)
			array.lazySet(i, 0);
		
		if (length > 0)
			array.set(0, 0);   // Flush
	}

}
