package bishop.controller;

public enum GameType {
	PLAY ("GameType.play"),
	ANALYSIS ("GameType.analysis"),
	ENDING_TRAINING ("GameType.endingTraining");
	
	private final String nameKey;
	
	private GameType (final String key) {
		this.nameKey = key;
	}
	
	/**
	 * Returns localized name of type.
	 * @param localization localization instance
	 * @return localized name of type
	 */
	public String getName(final ILocalization localization) {
		return localization.translateString(nameKey);
	}
}
