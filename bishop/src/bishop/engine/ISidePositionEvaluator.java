package bishop.engine;

import bishop.base.Position;

public interface ISidePositionEvaluator extends IPositionEvaluator {
	/**
	 * Returns evaluation of one side in given position.
	 * @param position position to evaluate
	 * @param color color of side to evaluate
	 * @return evaluation of given side from view of white side
	 */
	public int evaluateSide (final Position position, final int color);
}
