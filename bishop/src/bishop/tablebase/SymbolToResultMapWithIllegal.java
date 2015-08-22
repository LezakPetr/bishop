package bishop.tablebase;

public class SymbolToResultMapWithIllegal implements ISymbolToResultMap {
	
	private final ISymbolToResultMap baseMap;
	
	public SymbolToResultMapWithIllegal (final ISymbolToResultMap baseMap) {
		this.baseMap = baseMap;
	}
	
	/**
	 * Returns result for given symbol. 
	 * @param symbol symbol
	 * @return table result
	 */
	public int symbolToResult (final int symbol) {
		if (symbol == baseMap.getSymbolCount())
			return TableResult.ILLEGAL;
		else
			return baseMap.symbolToResult(symbol);
	}
	
	/**
	 * Returns symbol for given result. 
	 * @param result table result
	 * @return symbol
	 */
	public int resultToSymbol (final int result) {
		if (result == TableResult.ILLEGAL)
			return baseMap.getSymbolCount();
		else
			return baseMap.resultToSymbol(result);		
	}
	
	/**
	 * Returns number of symbols (and results).
	 * @return symbol count
	 */
	public int getSymbolCount() {
		return baseMap.getSymbolCount() + 1;
	}

}
