package bishop.engine;

import bishop.base.IPosition;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

		final List<TablePositionEvaluator> evaluators = IntStream.range(CombinedEvaluation.COMPONENT_FIRST, CombinedEvaluation.COMPONENT_LAST)
				.mapToObj(i -> new TablePositionEvaluator(coeffs.get(i), evaluationFactory))
				.collect(Collectors.toList());

		for (int gameStage = GameStage.FIRST; gameStage < GameStage.LAST; gameStage++) {
			final int gameStageFinal = gameStage;

			components[gameStage] = IntStream.range(CombinedEvaluation.COMPONENT_FIRST, CombinedEvaluation.COMPONENT_LAST)
					.mapToObj(i -> new Component(
							CombinedEvaluation.getComponentMultiplicator(gameStageFinal, i),
							evaluators.get(i)
					))
					.toArray(Component[]::new);
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
