package bishop.controller;

import org.w3c.dom.Element;

public class GuiSettings {
	
	private String language;
	
	private static final String ELEMENT_LANGUAGE = "language";
	public static final String DEFAULT_LANGUAGE = "english";
	
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(final String language) {
		this.language = language;
	}

	public void readFromXmlElement (final Element parentElement) {
		final Element elementLanguage = Utils.getElementByName(parentElement, ELEMENT_LANGUAGE);
		language = elementLanguage.getTextContent().trim();
	}

	public void writeToXmlElement (final Element parentElement) {
		final Element elementLanguage = Utils.addChildElement(parentElement, ELEMENT_LANGUAGE);
		elementLanguage.setTextContent(language);
	}

	public void setDefaults() {
		language = DEFAULT_LANGUAGE;
	}
	
	public void assign (final GuiSettings orig) {
		this.language = orig.language;
	}

	public GuiSettings copy() {
		final GuiSettings copyOfSettings = new GuiSettings();
		copyOfSettings.assign(this);
		
		return copyOfSettings;
	}

}
