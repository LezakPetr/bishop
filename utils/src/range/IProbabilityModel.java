package range;

/**
 * Probability model defines finite number of symbols in range
 * <0; getSymbolCount()) and assigns them values of cumulative distribution
 * function in range <0; RangeBase.MAX_SYMBOL_CDF) in form of intervals
 * <0; cdf_0), <cdf_0; cdf_1), ..., <cdf_(n-1); cdf_n), cdf_n = MAX_SYMBOL_CDF.
 * Width of intervals should be proportional to probability of symbol occurrence.
 * @author Ing. Petr Ležák
 */
public interface IProbabilityModel {
	/**
	 * Returns number of symbols in the model.
	 * @return number of symbols
	 */
	public int getSymbolCount();
	
	/**
	 * Returns lower bound of cumulative distribution function for given symbol.
	 * This is also upper bound for previous symbol.
	 * @param symbol symbol
	 * @return lower bound of CDF
	 */
	public int getCdfLowerBound (final int symbol);
	
	/**
	 * Returns some symbol for which getCdfLowerBound(symbol) <= cdf.
	 * For performance reasons method should return maximal symbol fulfilling
	 * that condition but it may also return lower symbol. 
	 * @param cdf cdf
	 * @return symbol
	 */
	public int getSymbolForCdf (final int cdf);
	
	/**
	 * Returns probability of give symbol multiplied by MAX_SYMBOL_CDF.
	 * @param symbol symbol
	 * @return probability * MAX_SYMBOL_CDF
	 */
	public default int getSymbolProbability(final int symbol) {
		return getCdfLowerBound(symbol + 1) - getCdfLowerBound(symbol);
	}
}
