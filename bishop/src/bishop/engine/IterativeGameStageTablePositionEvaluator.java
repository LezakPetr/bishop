package bishop.engine;

import bishop.base.IPosition;

import java.util.function.Supplier;

public class IterativeGameStageTablePositionEvaluator implements IGameStageTablePositionEvaluator {

	private final IPositionEvaluation evaluation;

	public IterativeGameStageTablePositionEvaluator(final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluation = evaluationFactory.get();
	}

	@Override
	public IPositionEvaluation evaluate(final IPosition position, final int gameStage) {
		evaluation.clear();
		evaluation.addEvaluation(position.getTablePositionEvaluation(gameStage));

		return evaluation;
	}
}
