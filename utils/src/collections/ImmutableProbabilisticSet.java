package collections;

import java.util.Collection;

import utils.IntUtils;

/**
 * Probabilistic immutable data structure that can answer a question if some element is contained
 * in it. The actual implementation is Bloom filter of order 1 with the size of power of two.  
 * @author Ing. Petr Ležák
 */
public class ImmutableProbabilisticSet<T> {

	private static final double DEFUALT_FALSE_POSITIVE_PROBABILITY = 0.01;
	private static final int INDEX_MASK = (1 << Long.SIZE) - 1;
	
	private final long[] data;
	private final int indexMask;
	
	public ImmutableProbabilisticSet(final Collection<? extends T> elements) {
		this (elements, DEFUALT_FALSE_POSITIVE_PROBABILITY);
	}
	
	public ImmutableProbabilisticSet(final Collection<? extends T> elements, final double falsePositiveProbabilitty) {
		final int bits = Math.max(IntUtils.ceilLog(elements.size() / falsePositiveProbabilitty), Long.SIZE);
		data = new long[1 << (bits - Long.SIZE)];
		indexMask = (1 << bits) - 1;
		
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
		final int index = hash & indexMask;
		
		return index;
	}

	private int getDataIndex(final int index) {
		return index >>> Long.SIZE;
	}

	private long getDataMask(final int index) {
		return 1L << (index & INDEX_MASK);
	}

}
