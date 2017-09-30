package utils;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

/**
 * INumberArray implementation that stores data in long direct buffers.
 * Numbers are stored in 8 byte words (longs) in chunks of elementBits bits.
 * The implementation ensures that after WORD_SIZE = 64 numbers the bit chunks are aligned to the words.
 * This means that if we logically divide the array into blocks with length multiple of 64 and let different
 * threads to write to different blocks they don't have to be synchronized.
 * More generally, for parallelism:
 * - reads doesn't have to be synchronized
 * - more writes or read + write doesn't have to be synchronized if they are in different WORD_SIZE blocks
 * 
 * @author Ing. Petr Ležák
 */
public class BitNumberArray implements INumberArray {
	
	private static final int WORD_SHIFT = 6;
	private static final int WORD_SIZE = 1 << WORD_SHIFT;
	private static final int WORD_MASK = WORD_SIZE - 1;
	
	private static final int PAGE_SHIFT = 24;
	private static final int PAGE_SIZE = 1 << PAGE_SHIFT;
	private static final int PAGE_MASK = PAGE_SIZE - 1;
	
	private final long size;
	private final int elementBits;
	private final LongBuffer[] data;
	
	public BitNumberArray (final long size, final int elementBits) {
		this.size = size;
		this.elementBits = elementBits;
		
		final long dataLength = IntUtils.divideRoundUp(size, WORD_SIZE) * elementBits;
		final int pageCount = (int) IntUtils.divideRoundUp(dataLength, PAGE_SIZE);

		this.data = new LongBuffer[pageCount];
		
		for (int i = 0; i < pageCount; i++) {
			final int pageDataLength = (int) Math.min(dataLength - (i << PAGE_SHIFT), PAGE_SIZE);
			
			this.data[i] = ByteBuffer.allocateDirect(Long.BYTES * pageDataLength).asLongBuffer();
		}
	}
	
	/**
	 * Returns maximal possible stored element (exclusive).
	 * @return maximal possible stored element
	 */
	public int getMaxElement() {
		return 1 << elementBits;
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
		final long bitOffset = index * elementBits;
		
		final long firstTotalWordIndex = bitOffset >>> WORD_SHIFT;
		final int firstPageIndex = (int) (firstTotalWordIndex >>> PAGE_SHIFT);
		final int firstWordIndex = (int) firstTotalWordIndex & PAGE_MASK;
		final int firstBitOffset = (int) (bitOffset & WORD_MASK);
		final int firstBitCount = Math.min(WORD_SIZE - firstBitOffset, elementBits);
		
		final long firstWord = data[firstPageIndex].get(firstWordIndex);
		final int firstElementMask = (1 << firstBitCount) - 1;
		int element = (int) (firstWord >>> firstBitOffset) & firstElementMask;

		final int secondBitCount = elementBits - firstBitCount;
		
		if (secondBitCount > 0) {
			final long secondTotalWordIndex = firstTotalWordIndex + 1;
			final int secondPageIndex = (int) (secondTotalWordIndex >>> PAGE_SHIFT);
			final int secondWordIndex = (int) secondTotalWordIndex & PAGE_MASK;
			
			final long secondWord = data[secondPageIndex].get(secondWordIndex);
			final int secondElementMask = (1 << secondBitCount) - 1;
			element |= ((int) secondWord & secondElementMask) << firstBitCount;
		}
		
		return element;
	}
	
	/**
	 * Sets element at given index
	 * @param index index
	 * @param element new value
	 */
	public void setAt (final long index, final int element) {
		final long bitOffset = index * elementBits;
		
		final long firstTotalWordIndex = bitOffset >>> WORD_SHIFT;
		final int firstPageIndex = (int) (firstTotalWordIndex >>> PAGE_SHIFT);
		final int firstWordIndex = (int) firstTotalWordIndex & PAGE_MASK;
		final int firstBitOffset = (int) (bitOffset & WORD_MASK);
		final int firstBitCount = Math.min(WORD_SIZE - firstBitOffset, elementBits);
		
		final long firstElementMask = (1 << firstBitCount) - 1;
		final long firstOrigWord = data[firstPageIndex].get(firstWordIndex);
		final long firstUpdatedWord = (firstOrigWord & ~(firstElementMask << firstBitOffset)) | ((element & firstElementMask) << firstBitOffset);
		data[firstPageIndex].put(firstWordIndex, firstUpdatedWord);

		final int secondBitCount = elementBits - firstBitCount;
		
		if (secondBitCount > 0) {
			final long secondTotalWordIndex = firstTotalWordIndex + 1;
			final int secondPageIndex = (int) (secondTotalWordIndex >>> PAGE_SHIFT);
			final int secondWordIndex = (int) secondTotalWordIndex & PAGE_MASK;

			final int secondElementMask = (1 << secondBitCount) - 1;
			final long secondOrigWord = data[secondPageIndex].get(secondWordIndex);
			final long secondUpdatedWord = (secondOrigWord & ~secondElementMask) | ((element >> firstBitCount) & secondElementMask);
			data[secondPageIndex].put(secondWordIndex, secondUpdatedWord);
		}
	}
}
