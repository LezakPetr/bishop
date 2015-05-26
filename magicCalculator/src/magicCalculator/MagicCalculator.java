package magicCalculator;

public class MagicCalculator {

	public static void main (final String[] args) {
		final MagicOptimizer optimizer = new MagicOptimizer();
		optimizer.fillFromLineIndexer();
		optimizer.optimize();
	}
}
