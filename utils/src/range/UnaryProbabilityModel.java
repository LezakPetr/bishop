package range;

public class UnaryProbabilityModel implements IProbabilityModel {

	private static final UnaryProbabilityModel INSTANCE = new UnaryProbabilityModel();
	
	
	private UnaryProbabilityModel() {
	}
	
	@Override
	public int getSymbolCount() {
		return 1;
	}

	@Override
	public int getCdfLowerBound(final int symbol) {
		switch (symbol) {
			case 0:
				return 0;
				
			case 1:
				return RangeBase.MAX_SYMBOL_CDF;
				
			default:
				throw new RuntimeException("Unknown symbol " + symbol);
		}
	}

	@Override
	public int getSymbolForCdf(final int cdf) {
		return 0;
	}

	public static UnaryProbabilityModel getInstance() {
		return INSTANCE;
	}
}
