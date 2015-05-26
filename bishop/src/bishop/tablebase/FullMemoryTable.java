package bishop.tablebase;


public class FullMemoryTable extends MemoryTable {

	private final short[] table;
	
	public FullMemoryTable(final TableDefinition definition) {
		this(definition, 0, Long.MAX_VALUE);
	}
	
	public FullMemoryTable(final TableDefinition definition, final long offset, final long size) {
		super (definition, offset, size);
		
		this.table = new short[(int) getItemCount()];
	}
	
	public int getResult(final long index) {
		if (index < 0)
			return TableResult.ILLEGAL;
		
		final int innerIndex = (int) getInnerIndex(index);
		
		return table[innerIndex];
	}

	public void setResult(final long index, final int result) {
		final int innerIndex = (int) getInnerIndex(index);
		
		table[innerIndex] = (short) result;
	}
	
}
