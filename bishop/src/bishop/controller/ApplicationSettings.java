package bishop.controller;

import org.w3c.dom.Element;

import bishop.base.Copyable;

public class ApplicationSettings implements Copyable<ApplicationSettings> {
	
	private final GameSettings gameSettings;
	private final EngineSettings engineSettings;
	private final GuiSettings guiSettings;
	
	private static final String ELEMENT_GAME_SETTINGS = "game_settings";
	private static final String ELEMENT_ENGINE_SETTINGS = "engine_settings";
	private static final String ELEMENT_GUI_SETTINGS = "gui_settings";
	
	
	public ApplicationSettings() {
		gameSettings = new GameSettings();
		engineSettings = new EngineSettings();
		guiSettings = new GuiSettings();
	}

	public GameSettings getGameSettings() {
		return gameSettings;
	}

	public EngineSettings getEngineSettings() {
		return engineSettings;
	}
	
	public GuiSettings getGuiSettings() {
		return guiSettings;
	}

	public void readFromXmlElement (final Element parentElement) {
		final Element elementGameSettings = Utils.getElementByName(parentElement, ELEMENT_GAME_SETTINGS);
		gameSettings.readFromXmlElement (elementGameSettings);
		
		final Element elementEngineSettings = Utils.getElementByName(parentElement, ELEMENT_ENGINE_SETTINGS);
		engineSettings.readFromXmlElement (elementEngineSettings);
		
		final Element elementGuiSettings = Utils.getElementByName(parentElement, ELEMENT_GUI_SETTINGS);
		guiSettings.readFromXmlElement (elementGuiSettings);
	}

	public void writeToXmlElement (final Element parentElement) {
		final Element elementGameSettings = Utils.addChildElement(parentElement, ELEMENT_GAME_SETTINGS);
		gameSettings.writeToXmlElement (elementGameSettings);
		
		final Element elementEngineSettings = Utils.addChildElement(parentElement, ELEMENT_ENGINE_SETTINGS);
		engineSettings.writeToXmlElement (elementEngineSettings);
		
		final Element elementGuiSettings = Utils.addChildElement(parentElement, ELEMENT_GUI_SETTINGS);
		guiSettings.writeToXmlElement (elementGuiSettings);
	}
	
	public void setDefaults() {
		gameSettings.setDefaults();
		engineSettings.setDefaults();
		guiSettings.setDefaults();
	}
	
	public void assign (final ApplicationSettings orig) {
		this.gameSettings.assign(orig.gameSettings);
		this.engineSettings.assign(orig.engineSettings);
		this.guiSettings.assign(orig.guiSettings);
	}

	public ApplicationSettings copy() {
		final ApplicationSettings copyOfSettings = new ApplicationSettings();
		copyOfSettings.assign(this);
		
		return copyOfSettings;
	}

}
