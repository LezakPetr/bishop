package range;

public class BinaryProbabilityModel implements IProbabilityModel {

	private final int treshold;
	
	BinaryProbabilityModel (final int treshold) {
		this.treshold = treshold;
	}
	
	@Override
	public int getSymbolCount() {
		return 2;
	}

	@Override
	public int getCdfLowerBound(final int symbol) {
		switch (symbol) {
			case 0:
				return 0;
				
			case 1:
				return treshold;
				
			case 2:
				return RangeBase.MAX_SYMBOL_CDF;
				
			default:
				throw new RuntimeException("Symbol out of range: " + symbol);
		}
	}

	@Override
	public int getSymbolForCdf(final int cdf) {
		return (cdf < treshold) ? 0 : 1;
	}

}
