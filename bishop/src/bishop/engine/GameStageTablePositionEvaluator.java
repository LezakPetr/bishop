package bishop.engine;

import bishop.base.IPosition;

import java.util.List;
import java.util.function.Supplier;

public class GameStageTablePositionEvaluator implements IGameStageTablePositionEvaluator {
	private class Component {
		final int coeff;
		final TablePositionEvaluator evaluator;

		public Component(final int coeff, final TablePositionEvaluator evaluator) {
			this.coeff = coeff;
			this.evaluator = evaluator;
		}
	}

	private final Component[][] components;
	private final IPositionEvaluation evaluation;


	public GameStageTablePositionEvaluator(final List<TablePositionCoeffs> coeffs, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluation = evaluationFactory.get();
		this.components = new Component[GameStage.LAST][];

		final TablePositionEvaluator endingEvaluator = new TablePositionEvaluator(coeffs.get(CombinedEvaluation.COMPONENT_ENDING), evaluationFactory);
		final TablePositionEvaluator openingEvaluator = new TablePositionEvaluator(coeffs.get(CombinedEvaluation.COMPONENT_OPENING), evaluationFactory);

		for (int i = GameStage.FIRST; i < GameStage.LAST; i++) {
			final int alpha = CombinedEvaluation.getAlphaForGameStage(i);

			components[i] = new Component[] {
					new Component(CombinedEvaluation.MAX_ALPHA - alpha, endingEvaluator),
					new Component(alpha, openingEvaluator),
			};
		}
	}

	@Override
	public IPositionEvaluation evaluate(final IPosition position, final int gameStage) {
		evaluation.clear();

		for (Component component : components[gameStage]) {
			final IPositionEvaluation subEvaluation = component.evaluator.evaluatePosition(position);
			evaluation.addSubEvaluation(subEvaluation, component.coeff);
		}

		evaluation.shiftRight(CombinedEvaluation.ALPHA_BITS);

		return evaluation;
	}

}
