package bishop.engine;

public final class PositionEvaluatorSwitchSettings {
	
	private final GeneralEvaluatorSettings generalEvaluatorSettings;
	
	public PositionEvaluatorSwitchSettings() {
		generalEvaluatorSettings = new GeneralEvaluatorSettings();
	}

	public GeneralEvaluatorSettings getGeneralEvaluatorSettings() {
		return generalEvaluatorSettings;
	}
}
