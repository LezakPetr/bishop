package bishop.engine;

public final class PositionEvaluatorSwitchSettings {
	
	private final MiddleGameEvaluatorSettings middleGameEvaluatorSettings;
	
	public PositionEvaluatorSwitchSettings() {
		middleGameEvaluatorSettings = new MiddleGameEvaluatorSettings();
	}

	public MiddleGameEvaluatorSettings getMiddleGameEvaluatorSettings() {
		return middleGameEvaluatorSettings;
	}
}
