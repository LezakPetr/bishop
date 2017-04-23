package range;

/**
 * Statistics that can be used to calculate probability model.
 * 
 * @author Ing. Petr Ležák
 */
public class ProbabilityStatistics {
	private final long[] symbolFrequencies;
	
	public ProbabilityStatistics(final int symbolCount) { 
		this.symbolFrequencies = new long[symbolCount];
	}
	
	public IProbabilityModel buildProbabilityModel() {
		return ProbabilityModelFactory.fromUnnormalizedProbabilities(symbolFrequencies);
	}
	
	public void addSymbol (final int symbol) {
		addSymbol(symbol, 1);
	}
	
	public void addSymbol (final int symbol, final long count) {
		symbolFrequencies[symbol] += count;
	}
}
