package bishop.controller;

import java.io.IOException;
import java.util.List;

public interface ILocalization {
	/**
	 * Returns available languages.
	 * @return list of available languages
	 */
	public List<String> getAvailableLanguages();

	/**
	 * Returns selected language.
	 * @return language name
	 */
	public String getLanguage();
	
	/**
	 * Loads given language.
	 * @param language language name
	 */
	public void setLanguage (final String language) throws IOException;
	
	/**
	 * Translates string.
	 * @param key key of the string
	 * @return string value
	 */
	public String translateString (final String key);
}
