package bishop.engine;


public class PositionEvaluatorSwitchFactory implements IPositionEvaluatorFactory {
	
	private final PositionEvaluatorSwitchSettings settings;

	public PositionEvaluatorSwitchFactory (final PositionEvaluatorSwitchSettings settings) {
		this.settings = settings;
	}
	
	@Override
	public IPositionEvaluator createEvaluator() {
		return new PositionEvaluatorSwitch(settings);
	}

}
