package bishop.tablebase;

import utils.BitNumberArray;
import utils.INumberArray;

public class CompressedMemoryTable extends MemoryTable {

	private final ISymbolToResultMap symbolToResultMap;
	private final INumberArray table;
	
	public CompressedMemoryTable(final TableDefinition definition, final ISymbolToResultMap symbolToResultMap) {
		this(definition, 0, Long.MAX_VALUE, symbolToResultMap);
	}
	
	public CompressedMemoryTable(final TableDefinition definition, final long offset, final long size, final ISymbolToResultMap symbolToResultMap) {
		super (definition, offset, size);
		
		this.symbolToResultMap = symbolToResultMap;
		
		final int elementBits = Integer.SIZE - Integer.numberOfLeadingZeros(symbolToResultMap.getSymbolCount() - 1);
		this.table = new BitNumberArray(getItemCount(), elementBits);
	}
	
	public int getResult(final long index) {
		if (index < 0)
			return TableResult.ILLEGAL;
		
		final long innerIndex = getInnerIndex(index);
		final int symbol = table.getAt(innerIndex);
		
		return symbolToResultMap.symbolToResult(symbol);
	}

	public void setResult(final long index, final int result) {
		final long innerIndex = getInnerIndex(index);
		final int symbol = symbolToResultMap.resultToSymbol(result); 
		
		table.setAt(innerIndex, symbol);
	}

}
