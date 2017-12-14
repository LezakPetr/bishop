package math;

import java.util.Arrays;

public class SparseVector extends AbstractVector {

	private static final double[] EMPTY_ELEMENTS = {};
	private static final int[] EMPTY_INDICES = {};
	private static final double ZERO = 0.0;
	
	private double[] elements;
	private int[] indices;
	private int nonZeroElementCount;
	private int dimension;
	
	SparseVector(final int dimension, final double[] elements, final int[] indices) {
		this.elements = elements;
		this.indices = indices;
		this.dimension = dimension;
	}
	
	SparseVector(final int dimension) {
		this (dimension, EMPTY_ELEMENTS, EMPTY_INDICES);
	}

	@Override
	public int getDimension() {
		return dimension;
	}

	@Override
	public double getElement(final int index) {
		final int sparseIndex = findSparseIndex(index);
		
		if (sparseIndex >= 0)
			return elements[sparseIndex];
		else
			return ZERO;
	}
	
	private int findSparseIndex(final int index) {
		int i = 0;
		
		while (i < nonZeroElementCount && indices[i] < index)
			i++;
		
		if (i < nonZeroElementCount && indices[i] == index)
			return i;
		else
			return -i - 1;
	}

	@Override
	public IVector setElement(final int index, final double value) {
		checkNotFrozen();
		
		final int sparseIndex = findSparseIndex(index);
		
		if (sparseIndex >= 0) {
			if (value == ZERO)
				removeElement(sparseIndex);
			else
				elements[sparseIndex] = value;
		}
		else {
			if (value != ZERO)
				addElement (-sparseIndex - 1, index, value);
		}
		
		return this;
	}

	private void addElement(final int sparseIndex, final int index, final double value) {
		final double[] origElements = elements;
		final int[] origIndices = indices;
		
		final int origSize = nonZeroElementCount;
		nonZeroElementCount++;
		
		if (elements.length < nonZeroElementCount) {
			final int newSize = Math.max(nonZeroElementCount, 2*elements.length);
			
			elements = new double[newSize];
			System.arraycopy(origElements, 0, elements, 0, sparseIndex);
			
			indices = new int[newSize];
			System.arraycopy(origIndices, 0, indices, 0, sparseIndex);
		}
		
		System.arraycopy(origElements, sparseIndex, elements, sparseIndex + 1, origSize - sparseIndex);
		elements[sparseIndex] = value;
		
		System.arraycopy(origIndices, sparseIndex, indices, sparseIndex + 1, origSize - sparseIndex);
		indices[sparseIndex] = index;
	}

	private void removeElement(final int sparseIndex) {
		nonZeroElementCount--;
		
		System.arraycopy(elements, sparseIndex + 1, elements, sparseIndex, nonZeroElementCount - sparseIndex);
		System.arraycopy(indices, sparseIndex + 1, indices, sparseIndex, nonZeroElementCount - sparseIndex);
	}

	@Override
	public Density density() {
		return Density.SPARSE;
	}

	@Override
	public IVectorIterator getNonZeroElementIterator() {
		return new SparseNonZeroElementIterator(this);
	}
	
	public int getNonZeroElementCount() {
		return nonZeroElementCount;
	}
	
	public double getNonZeroElement (final int sparseIndex) {
		return elements[sparseIndex];
	}

	public int getNonZeroIndex (final int sparseIndex) {
		return indices[sparseIndex];
	}
	

	@Override
	public void assign(final IVectorRead orig) {
		if (this.getDimension() != orig.getDimension())
			throw new RuntimeException("Different dimensions of vectors");
		
		if (orig instanceof SparseVector) {
			final SparseVector origSparse = (SparseVector) orig;
			
			this.nonZeroElementCount = origSparse.nonZeroElementCount;
			this.elements = Arrays.copyOf(origSparse.elements, nonZeroElementCount);
			this.indices = Arrays.copyOf(origSparse.indices, nonZeroElementCount);
		}
		else
			super.assign(orig);
	}
	
	public void swap(final SparseVector that) {
		if (this.getDimension() != that.getDimension())
			throw new RuntimeException("Different dimensions of vectors");

		final int tmpNonZeroElementCount = this.nonZeroElementCount;
		this.nonZeroElementCount = that.nonZeroElementCount;
		that.nonZeroElementCount = tmpNonZeroElementCount;
		
		final double[] tmpElements = this.elements;
		this.elements = that.elements;
		that.elements = tmpElements;
		
		final int[] tmpIndices = this.indices;
		this.indices = that.indices;
		that.indices = tmpIndices;
	}

}
