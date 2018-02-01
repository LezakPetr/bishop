package bishop.engine;


public class PositionEvaluatorSwitchFactory implements IPositionEvaluatorFactory {
	
	private final PositionEvaluatorSwitchSettings settings;

	public PositionEvaluatorSwitchFactory (final PositionEvaluatorSwitchSettings settings) {
		this.settings = settings;
	}
	
	@Override
	public IPositionEvaluator createEvaluator(final IPositionEvaluation evaluation) {
		return new PositionEvaluatorSwitch(settings, evaluation);
	}

}
