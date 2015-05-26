package bishop.controller;

public enum SideType {
	HUMAN ("SideType.human"),
	COMPUTER("SideType.computer");
	
	private final String nameKey;
	
	private SideType (final String key) {
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
