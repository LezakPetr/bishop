package bishop.engine;

import java.util.function.Supplier;

public class PositionEvaluatorSwitchFactory implements IPositionEvaluatorFactory {

	private final Supplier<IPositionEvaluation> evaluationFactory;

	public PositionEvaluatorSwitchFactory (final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluationFactory = evaluationFactory;
	}
	
	@Override
	public IPositionEvaluator createEvaluator() {
		return new PositionEvaluatorSwitch(evaluationFactory);
	}

}
