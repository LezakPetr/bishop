package bishop.tablebase;

import bishop.base.Position;

public class ClassificationTableAdaptor implements ITableRead {
	
	private final ITableRead baseTable;
	private final long offset;
	private final long size;
	
	public ClassificationTableAdaptor(final ITableRead baseTable, final long offset, final long size) {
		this.baseTable = baseTable;
		this.offset = offset;
		this.size = size;
	}
	
	@Override
	public int getPositionResult(final Position position) {
		return baseTable.getPositionResult(position);
	}

	@Override
	public ITableIteratorRead getIterator() {
		return new TableIteratorReadImpl(this, offset);
	}

	@Override
	public TableDefinition getDefinition() {
		return baseTable.getDefinition();
	}

	@Override
	public int getResult(final long index) {
		if (index < 0)
			return TableResult.ILLEGAL;
		
		if (offset >= size)
			throw new IllegalArgumentException("Index out of range: " + index);
		
		return baseTable.getResult(index + size);
	}

}
