package range;

public abstract class ProbabilityModelImpl implements IProbabilityModel {

	public int valueToSymbol(final int value) {
		int low = 0;
		int high = getSymbolCount();
		
		while (high - low > 1) {
			final int middle = (high + low) / 2;
			
			if (value >= getCdfLowerBound(middle))
				low = middle;
			else
				high = middle;
		}
		
		return low;
	}
}
