package bishop.base;


public interface IMaterialEvaluator {
	/**
	 * Returns absolute evaluation for given piece counts.
	 * @param materialHash material hash
	 * @return evaluation from white point of view
	 */
	public int evaluateMaterial (final IMaterialHashRead materialHash);
}
