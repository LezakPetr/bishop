package bishop.controller;

import org.w3c.dom.Element;

import bishop.base.Color;
import bishop.base.Copyable;

public class GameSettings implements Copyable<GameSettings> {

	private GameType gameType;
	private final SideSettings[] sideSettings;
	
	private static final String ELEMENT_GAME_TYPE = "game_type";
	private static final String ELEMENT_WHITE = "white";
	private static final String ELEMENT_BLACK = "black";
	
	private static final GameType DEFAULT_GAME_TYPE = GameType.PLAY;
	
	public GameSettings() {
		gameType = DEFAULT_GAME_TYPE;
		sideSettings = new SideSettings[Color.LAST];
		
		for (int color = 0; color < Color.LAST; color++)
			sideSettings[color] = new SideSettings();
	}
	
	public SideSettings getSideSettings (final int color) {
		return sideSettings[color];
	}
	
	public GameType getGameType() {
		return gameType;
	}

	public void setGameType(final GameType gameType) {
		this.gameType = gameType;
	}

	public void assign (final GameSettings orig) {
		this.gameType = orig.gameType;
		
		for (int color = 0; color < Color.LAST; color++)
			this.sideSettings[color].assign (orig.sideSettings[color]);
	}
	
	public GameSettings copy() {
		final GameSettings copy = new GameSettings();
		copy.assign(this);
		
		return copy;
	}
	
	public void setDefaults() {
		gameType = DEFAULT_GAME_TYPE;
		
		final SideSettings whiteSettings = getSideSettings(Color.WHITE);
		whiteSettings.setSideType(SideType.HUMAN);
		whiteSettings.setTimeForMove(10000);
		
		final SideSettings blackSettings = getSideSettings(Color.BLACK);
		blackSettings.setSideType(SideType.COMPUTER);
		blackSettings.setTimeForMove(10000);
	}
	
	public void readFromXmlElement (final Element parentElement) {
		final Element elementGameType = Utils.getElementByName(parentElement, ELEMENT_GAME_TYPE);
		gameType = GameType.valueOf(elementGameType.getTextContent());
		
		final Element elementWhiteSettings = Utils.getElementByName(parentElement, ELEMENT_WHITE);
		getSideSettings(Color.WHITE).readFromXmlElement (elementWhiteSettings);
		
		final Element elementBlackSettings = Utils.getElementByName(parentElement, ELEMENT_BLACK);
		getSideSettings(Color.BLACK).readFromXmlElement (elementBlackSettings);
	}

	public void writeToXmlElement (final Element parentElement) {
		final Element elementGameType = Utils.addChildElement(parentElement, ELEMENT_GAME_TYPE);
		elementGameType.setTextContent(gameType.toString());

		final Element elementWhiteSettings = Utils.addChildElement(parentElement, ELEMENT_WHITE);
		getSideSettings(Color.WHITE).writeToXmlElement (elementWhiteSettings);
		
		final Element elementBlackSettings = Utils.addChildElement(parentElement, ELEMENT_BLACK);
		getSideSettings(Color.BLACK).writeToXmlElement (elementBlackSettings);
	}

}
