package bishop.tablebase;

public class CompressedMemoryTable extends MemoryTable {

	private final byte[] table;
	
	public CompressedMemoryTable(final TableDefinition definition) {
		this(definition, 0, Long.MAX_VALUE);
	}
	
	public CompressedMemoryTable(final TableDefinition definition, final long offset, final long size) {
		super (definition, offset, size);
		
		this.table = new byte[(int) getItemCount()];
	}
	
	public int getResult(final long index) {
		if (index < 0)
			return TableResult.ILLEGAL;
		
		final int innerIndex = (int) getInnerIndex(index);
		final byte compressedResult = table[innerIndex];
		
		return TableResult.decompress(compressedResult);
	}

	public void setResult(final long index, final int result) {
		final int innerIndex = (int) getInnerIndex(index);
		final byte compressedResult = TableResult.compress(result);
		
		table[innerIndex] = compressedResult;
	}

}
