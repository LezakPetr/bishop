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
		return 0;
	}

	@Override
	public int getSymbolForCdf(final int cdf) {
		return 0;
	}

	public static UnaryProbabilityModel getInstance() {
		return INSTANCE;
	}
}
