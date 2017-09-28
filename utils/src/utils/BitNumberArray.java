package utils;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

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
	
	private static final int PAGE_SHIFT = 24;
	private static final int PAGE_SIZE = 1 << PAGE_SHIFT;
	private static final int PAGE_MASK = PAGE_SIZE - 1;
	
	private final long size;
	private final int elementBits;
	private final LongBuffer[][] data;
	
	public BitNumberArray (final long size, final int elementBits) {
		this.size = size;
		this.elementBits = elementBits;
		
		final long dataLength = (size + WORD_MASK) >>> WORD_SHIFT;
		final int pageCount = (int) IntUtils.divideRoundUp(dataLength, PAGE_SIZE);

		this.data = new LongBuffer[pageCount][elementBits];
		
		for (int i = 0; i < pageCount; i++) {
			final int pageDataLength = (int) Math.min(dataLength - (i << PAGE_SHIFT), PAGE_SIZE);
			
			for (int j = 0; j < elementBits; j++)
				this.data[i][j] = ByteBuffer.allocateDirect(Long.BYTES * pageDataLength).asLongBuffer();
		}
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
		
		final int pageIndex = (int) (index >>> (PAGE_SHIFT + WORD_SHIFT));
		final int wordIndex = ((int) (index >>> WORD_SHIFT)) & PAGE_MASK;
		final int bitIndex = ((int) index) & WORD_MASK;
		
		final LongBuffer[] page = data[pageIndex];
		
		for (int i = elementBits - 1; i >= 0; i--) {
			element = (element << 1) | ((int) (page[i].get(wordIndex) >>> bitIndex) & 0x01);
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

		final int pageIndex = (int) (index >>> (PAGE_SHIFT + WORD_SHIFT));
		final int wordIndex = ((int) (index >>> WORD_SHIFT)) & PAGE_MASK;
		final int bitIndex = ((int) index) & WORD_MASK; 
		final long mask = ~(1L << bitIndex); 
		
		final LongBuffer[] page = data[pageIndex];
		
		for (int i = 0; i < elementBits; i++) {
			page[i].put(wordIndex, (page[i].get(wordIndex) & mask) | ((long) (tmp & 0x01) << bitIndex));
			tmp >>>= 1;
		}
	}
}
