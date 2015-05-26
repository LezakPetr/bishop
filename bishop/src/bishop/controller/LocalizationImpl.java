package bishop.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import utils.Logger;
import utils.StringMap;


public class LocalizationImpl implements ILocalization {
	
	private final StringMap stringMap;
	private final URL directory;
	private String language;
	
	private static final String SUFFIX = ".lng";
	private static final String LISTING_FILE = "languages.lst";
	
	public LocalizationImpl (final URL directory) {
		this.stringMap = new StringMap();
		this.directory = directory;
	}
	
	/**
	 * Returns available languages.
	 * @return list of available languages
	 * @throws IOException 
	 */
	public List<String> getAvailableLanguages() {
		final List<String> languageList = new LinkedList<String>();
		
		try {
			final URL listingUrl = new URL (directory, LISTING_FILE);
			
			try (
				final InputStream stream = listingUrl.openStream();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			)
			{
				while (true) {
					final String str = reader.readLine();
					
					if (str == null)
						break;
					
					languageList.add(str);
				}
			}
		}
		catch (IOException ex) {
			Logger.logException(ex);
		}
		
		return languageList;
	}

	/**
	 * Returns selected language.
	 * @return language name
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Loads given language.
	 * @param language language name
	 * @throws IOException 
	 */
	public void setLanguage (final String language) throws IOException {
		this.language = language;
		
		final URL url = new URL(directory, language + SUFFIX);
		final InputStream stream = url.openStream();
		
		try {
			stringMap.readFromStream(stream);
		}
		finally {
			stream.close();
		}
	}
	
	/**
	 * Translates string.
	 * @param key key of the string
	 * @return string value
	 */
	public String translateString (final String key) {
		final String value = stringMap.getItem(key);
		
		if (value == null) {
			System.err.println ("Unlocalized string with key '" + key + "'");
			return key;
		}
		else
			return value;
	}	
}
