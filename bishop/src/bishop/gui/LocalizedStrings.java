package bishop.gui;

import bishop.controller.ILocalization;

public class LocalizedStrings {
	
	private static final String[] COLOR_KEYS = {"Color.white", "Color.black"};
	
	public static final String BUTTON_OK_KEY = "Button.ok.text";
	public static final String BUTTON_CANCEL_KEY = "Button.cancel.text";
	
	/**
	 * Returns localized name of color.
	 * @param localization localization instance
	 * @param color color
	 * @return name of the color
	 */
	public static final String translateColor (final ILocalization localization, final int color) {
		return localization.translateString(COLOR_KEYS[color]);
	}
}
