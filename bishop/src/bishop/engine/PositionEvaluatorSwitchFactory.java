package bishop.engine;

import java.util.function.Supplier;

public class PositionEvaluatorSwitchFactory implements IPositionEvaluatorFactory {
	
	private final PositionEvaluatorSwitchSettings settings;
	private final Supplier<IPositionEvaluation> evaluationFactory;

	public PositionEvaluatorSwitchFactory (final PositionEvaluatorSwitchSettings settings, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.settings = settings;
		this.evaluationFactory = evaluationFactory;
	}
	
	@Override
	public IPositionEvaluator createEvaluator() {
		return new PositionEvaluatorSwitch(settings, evaluationFactory);
	}

}
