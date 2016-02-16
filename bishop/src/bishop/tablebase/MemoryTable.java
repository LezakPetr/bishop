package bishop.tablebase;

import bishop.base.IPosition;

public abstract class MemoryTable implements ITable {
	
	private final TableDefinition definition;
	private final long offset;
	private final long count;
	
	protected MemoryTable(final TableDefinition definition, final long offset, final long size) {
		this.definition = definition;
		this.offset = offset;
		this.count = Math.min(definition.getTableIndexCount(), size);
	}

	public ITableIterator getIterator() {
		return new TableIteratorImpl(this, offset);
	}

	@Override
	public int getPositionResult(final IPosition position) {
		final long index = definition.calculateTableIndex(position);
		
		return getResult (index);
	}

	public TableDefinition getDefinition() {
		return definition;
	}
	
	public long getItemCount() {
		return count;
	}
	
	protected long getInnerIndex (final long tableIndex) {
		if (tableIndex < offset || tableIndex >= offset + count)
			throw new RuntimeException("Table index out of range: " + tableIndex);
		
		return tableIndex - offset;
	}
}
