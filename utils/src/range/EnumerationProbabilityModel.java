package range;


public class EnumerationProbabilityModel implements IProbabilityModel {
	
	private static final int SYMBOL_MASK = RangeBase.MAX_SYMBOL_CDF - 1;
	private static final int SYMBOLS_BY_CDF_BITS = 10;
	private static final int SYMBOLS_BY_CDF_COUNT = 1 << SYMBOLS_BY_CDF_BITS;
	private static final int SYMBOLS_BY_CDF_SHIFT = RangeBase.MAX_SYMBOL_BITS - SYMBOLS_BY_CDF_BITS;
	
	private final int[] cdf;   // Cumulative distribution function
	private final short[] symbolsByCdf;   // Lowest symbol sharing some bit pattern 
	
	public EnumerationProbabilityModel(final int[] symbolProbabilities) {
		cdf = createCdf(symbolProbabilities);
		symbolsByCdf = createSymbolsByCdf();
	}
	
	private int[] createCdf(final int[] symbolProbabilities) {
		final int symbolCount = symbolProbabilities.length;
		final int[] newCdf = new int[symbolCount + 1];
		
		newCdf[0] = 0;
		
		for (int i = 0; i < symbolCount; i++)
			newCdf[i+1] = newCdf[i] + symbolProbabilities[i];	
		
		if (newCdf[symbolCount] != RangeBase.MAX_SYMBOL_CDF)
			throw new RuntimeException("Wrong max symbol value");
		
		return newCdf;
	}
	
	private short[] createSymbolsByCdf() {
		final short[] newSymbolsByCdf = new short[SYMBOLS_BY_CDF_COUNT];
		int minSymbol = 0;
		
		for (int i = 0; i < SYMBOLS_BY_CDF_COUNT; i++) {
			while (cdf[minSymbol+1] <= (i << SYMBOLS_BY_CDF_SHIFT))
				minSymbol++;
			
			newSymbolsByCdf[i] = (short) minSymbol;
		}
		
		return newSymbolsByCdf;
	}
	
	public int getSymbolCount() {
		return cdf.length - 1;
	}

	public int getCdfLowerBound(final int symbol) {
		return cdf[symbol];
	}
	
	public int getSymbolForCdf (final int cdf) {
		final int index = cdf >>> SYMBOLS_BY_CDF_SHIFT;
		final int symbol = symbolsByCdf[index];
		
		return symbol & SYMBOL_MASK;
	}
	
	public int[] getSymbolProbabilities() {
		final int symbolCount = getSymbolCount();
		final int[] symbolProbabilities = new int[symbolCount];
		
		for (int i = 0; i < symbolProbabilities.length; i++)
			symbolProbabilities[i] = cdf[i+1] - cdf[i];
		
		return symbolProbabilities;
	}
	
	/**
	 * Calculates probability array from given frequency array.
	 * @param symbolFrequencies array of symbol frequencies
	 * @return probability array
	 */
	public static int[] calculateSymbolProbabilities(final long[] symbolFrequencies) {
		long frequencySum = 0;
		
		for (long frequency: symbolFrequencies)
			frequencySum += frequency;
		
		final int symbolCount = symbolFrequencies.length;
		final int[] symbolProbabilities = new int[symbolCount];
		int remainingProbabilityRange = RangeBase.MAX_SYMBOL_CDF;
		
		for (int i = 0; i < symbolCount; i++) {
			final long frequency = symbolFrequencies[i];
			final double dblProbability = (double) frequency * (double) remainingProbabilityRange / (double) frequencySum;
			int probability = (int) Math.round(dblProbability);
			
			final int remainingSymbolCount = symbolCount - i - 1;
			probability = Math.min(probability, remainingProbabilityRange - remainingSymbolCount);
			probability = Math.max (probability, RangeBase.MIN_SYMBOL_PROBABILITY);
			
			symbolProbabilities[i] = probability;
			
			remainingProbabilityRange -= probability;
			frequencySum -= frequency;
		}
		
		symbolProbabilities[symbolProbabilities.length - 1] += remainingProbabilityRange;
		
		return symbolProbabilities;
	}
}
