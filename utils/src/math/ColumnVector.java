package math;

public class ColumnVector implements IVector {

	private final IMatrix baseMatrix;
	private final int rowOffset;
	private final int rowDimension;
	private final int column;
	
	public ColumnVector(final IMatrix baseMatrix, final int rowOffset, final int rowDimension, final int column) {
		this.baseMatrix = baseMatrix;
		this.rowOffset = rowOffset;
		this.rowDimension = rowDimension;
		this.column = column;
	}
	
	@Override
	public int getDimension() {
		return rowDimension;
	}

	@Override
	public double getElement(final int index) {
		return baseMatrix.getElement(getSubIndex(index), column);
	}
	
	private int getSubIndex(final int index) {
		if (index < 0 || index >= rowDimension)
			throw new IndexOutOfBoundsException("Index out of bound " + index);
		
		return index + rowOffset;
	}

	@Override
	public IVector setElement(final int index, final double value) {
		baseMatrix.setElement(getSubIndex(index), column, value);
		
		return this;
	}

	@Override
	public IVectorRead freeze() {
		throw new RuntimeException("Cannot freeze sub vector");
	}

	@Override
	public Density density() {
		return Density.DENSE;
	}

	@Override
	public IVectorIterator getNonZeroElementIterator() {
		return new DenseNonZeroElementIterator(this);
	}
	
}
