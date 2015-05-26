package bishop.engine;

import bishop.base.Color;
import bishop.base.Position;

public abstract class SidePositionEvaluatorBase implements ISidePositionEvaluator {
	/**
	 * Returns evaluation of given position.
	 * @param position position to evaluate
	 * @return evaluation from view of white side
	 */
	public int evaluatePosition (final Position position) {
		int evaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++)
			evaluation += evaluateSide(position, color);
		
		return evaluation;
	}

}
