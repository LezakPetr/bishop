package bishop.engine;

import bishop.base.IPosition;

public interface IGameStageTablePositionEvaluator {
	public IPositionEvaluation evaluate(final IPosition position, final int gameStage);
}
