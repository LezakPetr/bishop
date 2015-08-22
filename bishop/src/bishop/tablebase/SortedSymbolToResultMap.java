package bishop.tablebase;

import java.util.Arrays;

public class SortedSymbolToResultMap implements ISymbolToResultMap {
	
	private final short[] symbolToResultTable;
	
	
	public SortedSymbolToResultMap(final int[] symbolToResultTable) {
		this.symbolToResultTable = Utils.intArrayToShort(symbolToResultTable);
		
		verifyTableSorted();
	}
	
	private void verifyTableSorted() {
		for (int i = 1; i < symbolToResultTable.length; i++) {
			if (symbolToResultTable[i-1] >= symbolToResultTable[i])
				throw new RuntimeException("Symbol to result table is not sorted");
		}
	}
	
	/**
	 * Returns result for given symbol. 
	 * @param symbol symbol
	 * @return table result
	 */
	public int symbolToResult (final int symbol) {
		return symbolToResultTable[symbol];
	}
	
	/**
	 * Returns symbol for given result. 
	 * @param result table result
	 * @return symbol
	 */
	public int resultToSymbol (final int result) {
		final int index = Arrays.binarySearch(symbolToResultTable, (short) result);
		
		if (index >= 0)
			return index;
		else
			throw new RuntimeException("Cannot found result " + result);
	}
	
	/**
	 * Returns number of symbols (and results).
	 * @return symbol count
	 */
	public int getSymbolCount() {
		return symbolToResultTable.length;
	}
}
