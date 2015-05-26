package bishop.engine;

public final class PositionEvaluatorSwitchSettings {
	
	private final MiddleGameEvaluatorSettings middleGameEvaluatorSettings;
	private final EndingPositionEvaluatorSettings endingPositionEvaluatorSettings;
	
	public PositionEvaluatorSwitchSettings() {
		middleGameEvaluatorSettings = new MiddleGameEvaluatorSettings();
		endingPositionEvaluatorSettings = new EndingPositionEvaluatorSettings();
	}

	public MiddleGameEvaluatorSettings getMiddleGameEvaluatorSettings() {
		return middleGameEvaluatorSettings;
	}

	public EndingPositionEvaluatorSettings getEndingPositionEvaluatorSettings() {
		return endingPositionEvaluatorSettings;
	}
}
