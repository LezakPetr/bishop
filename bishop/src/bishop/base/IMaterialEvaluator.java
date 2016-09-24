package bishop.base;

public interface IMaterialEvaluator {
	/**
	 * Returns absolute evaluation for given piece counts.
	 * @param pieceCounts piece counts
	 * @return evaluation from white point of view
	 */
	public int evaluateMaterial (final IPieceCounts pieceCounts);
}
