package utils;

/**
 * INumberArray implementation that stores data in bit slices.
 * Numbers are stored in 8 byte words in bit slices. Reads and writes to different words are independent.
 * This means that if we logically divide the array into block with length multiple of 64 and let different
 * threads to write to different blocks they don't have to be synchronized.
 * More generally, for parallelism:
 * - reads doesn't have to be synchronized
 * - more writes or read + write doesn't have to be synchronized if they are in different words
 * 
 * @author Ing. Petr Ležák
 */
public class BitNumberArray implements INumberArray {
	
	private static final int WORD_SHIFT = 6;
	private static final int WORD_MASK = (1 << WORD_SHIFT) - 1;
	
	private final long size;
	private final long[][] data;
	
	public BitNumberArray (final long size, final int elementBits) {
		this.size = size;
		
		final long dataLength = (size + WORD_MASK) >>> WORD_SHIFT;

		if (dataLength > Integer.MAX_VALUE)
			throw new RuntimeException("Size of BitNumberArray too big");

		this.data = new long[elementBits][(int) dataLength];
	}
	
	/**
	 * Returns maximal possible stored element (exclusive).
	 * @return maximal possible stored element
	 */
	public int getMaxElement() {
		return 1 << data.length;
	}
	
	/**
	 * Returns number of elements
	 * @return size
	 */
	public long getSize() {
		return size;
	}
	
	/**
	 * Returns element at given index.
	 * @param index index of element
	 * @return value
	 */
	public int getAt (final long index) {
		int element = 0;
		
		final int wordIndex = (int) (index >>> WORD_SHIFT);
		final int bitIndex = ((int) index) & WORD_MASK; 
		
		for (int i = data.length - 1; i >= 0; i--) {
			element = (element << 1) | ((int) (data[i][wordIndex] >>> bitIndex) & 0x01);
		}
		
		return element;
	}
	
	/**
	 * Sets element at given index
	 * @param index index
	 * @param element new value
	 */
	public void setAt (final long index, final int element) {
		int tmp = element;

		final int wordIndex = (int) (index >>> WORD_SHIFT);
		final int bitIndex = ((int) index) & WORD_MASK; 
		final long mask = ~(1L << bitIndex); 
		
		for (int i = 0; i < data.length; i++) {
			data[i][wordIndex] = (data[i][wordIndex] & mask) | ((long) (tmp & 0x01) << bitIndex);
			tmp >>>= 1;
		}
	}
}
