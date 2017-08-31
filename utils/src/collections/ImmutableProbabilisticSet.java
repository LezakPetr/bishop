package collections;

import java.util.Collection;

import utils.IntUtils;

/**
 * Probabilistic immutable data structure that can answer a question if some element is contained
 * in it. The actual implementation is Bloom filter of order 1 with the size of power of two.  
 * @author Ing. Petr Ležák
 */
public class ImmutableProbabilisticSet<T> {

	private static final long HASH_CODE_COEFF = 0xf37ba6b080d03acdL;

	private static final double DEFUALT_FALSE_POSITIVE_PROBABILITY = 0.01;
	
	private static final int DATA_INDEX_SHIFT = 6;
	private static final int DATA_INDEX_MASK = 0x3F;
	
	
	private final long[] data;
	private final int indexShift;
	
	public ImmutableProbabilisticSet(final Collection<? extends T> elements) {
		this (elements, DEFUALT_FALSE_POSITIVE_PROBABILITY);
	}
	
	public ImmutableProbabilisticSet(final Collection<? extends T> elements, final double falsePositiveProbabilitty) {
		final int bits = Math.max(IntUtils.ceilLog(elements.size() / falsePositiveProbabilitty), DATA_INDEX_SHIFT);
		data = new long[1 << (bits - DATA_INDEX_SHIFT)];
		indexShift = Long.SIZE - bits;
		
		for (T element: elements)
			addElement (element);
	}
	
	private void addElement (final T element) {
		final int index = getIndex(element);
		final int dataIndex = getDataIndex (index);
		final long dataMask = getDataMask (index);
		
		data[dataIndex] |= dataMask;
	}
	
	/**
	 * Checks if given element is contained in the set.
	 * @param element element
	 * @return false if the element is not contained in the set;
	 *         true if the element is contained in the set or with falsePositiveProbabilitty it is not contained
	 */
	public boolean contains (final T element) {
		final int index = getIndex(element);
		final int dataIndex = getDataIndex (index);
		final long dataMask = getDataMask (index);
		
		return (data[dataIndex] & dataMask) != 0;
	}
	
	private int getIndex (final T element) {
		final int hash = element.hashCode();
		final int index = (int) ((hash * HASH_CODE_COEFF) >>> indexShift);
		
		return index;
	}

	private int getDataIndex(final int index) {
		return index >>> DATA_INDEX_SHIFT;
	}

	private long getDataMask(final int index) {
		return 1L << (index & DATA_INDEX_MASK);
	}

}
