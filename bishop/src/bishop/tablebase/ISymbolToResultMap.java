package bishop.tablebase;

/**
 * Bidirectional mapping between symbols and table results.
 * Symbols are numbers in range 0 <= symbol < symbol count. 
 * Results are numbers specified in TableResult class.
 * Please note that both symbols and results can be stored in short,
 * but for convenience methods works with ints.
 * 
 * @author Ing. Petr Ležák
 */
public interface ISymbolToResultMap {
	/**
	 * Returns result for given symbol. 
	 * @param symbol symbol
	 * @return table result
	 */
	public int symbolToResult (final int symbol);
	
	/**
	 * Returns symbol for given result. 
	 * @param result table result
	 * @return symbol
	 */
	public int resultToSymbol (final int result);
	
	/**
	 * Returns number of symbols (and results).
	 * @return symbol count
	 */
	public int getSymbolCount();
}
