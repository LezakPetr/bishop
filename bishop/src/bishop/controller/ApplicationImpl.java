package bishop.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import utils.Logger;
import bishop.base.Color;
import bishop.base.GameHeader;
import bishop.base.GlobalSettings;
import bishop.base.HandlerRegistrarImpl;
import bishop.base.MaterialHash;
import bishop.base.Position;
import bishop.engine.TablebasePositionEvaluator;
import bishop.tablebase.ITableIteratorRead;
import bishop.tablebase.ITableRead;
import bishop.tablebase.TableDefinition;
import bishop.tablebase.TableResult;

public class ApplicationImpl implements IApplication {
	
	private final URL rootUrl;

	private final ApplicationSettings settings;
	private final HandlerRegistrarImpl<IApplicationListener> applicationListenerRegistrar;
	private GameEditor gameEditor;

	private ILocalization localization;
	private LocalizedComponentRegisterImpl localizedComponentRegister;
	private SearchResources searchResources;
	
	private RegimePlay regimePlay;
	private RegimeEditPosition regimeEditPosition;
	private RegimeAnalysis regimeAnalysis;
	private RegimeEndingTraining regimeEndingTraining;
	private IRegime actualRegime;

	private static final String SETTINGS_PATH = "settings.xml";
	private static final String SETTINGS_ELEMENT = "settings";
	
	private static final String LANGUAGES_DIRECTORY = "languages/";
	
	public ApplicationImpl (final URL rootUrl) throws IOException {
		this.rootUrl = rootUrl;
		this.applicationListenerRegistrar = new HandlerRegistrarImpl<IApplicationListener>();
		
		settings = new ApplicationSettings();
		
		localization = new LocalizationImpl(new URL(rootUrl, LANGUAGES_DIRECTORY));
		localization.setLanguage(GuiSettings.DEFAULT_LANGUAGE);
		localizedComponentRegister = new LocalizedComponentRegisterImpl(localization);
		
		loadSettings();
	}
	
	public void initialize() {
		gameEditor = new GameEditor();
		gameEditor.getGameListenerRegistrar();

		gameEditor.newGame(Position.INITIAL_POSITION);
		
		regimePlay = new RegimePlay(this);
		regimeEditPosition = new RegimeEditPosition(this);
		regimeAnalysis = new RegimeAnalysis(this);
		regimeEndingTraining = new RegimeEndingTraining(this);
	}

	/**
	 * Returns root URL of the application.
	 * @return URL
	 */
	public URL getRootUrl() {
		return rootUrl;
	}

	/*
	 * Returns editor of actual game.
	 * @return game editor
	 */
	public GameEditor getActualGameEditor() {
		return gameEditor;
	}

	/**
	 * Returns type of actual regime.
	 * @return regime type
	 */
	public RegimeType getRegimeType() {
		return actualRegime.getRegimeType();
	}
	
	/**
	 * Returns regime play.
	 * @return regime
	 */
	public RegimePlay getRegimePlay() {
		return regimePlay;
	}

	/**
	 * Returns regime analysis.
	 * @return regime
	 */
	public RegimeAnalysis getRegimeAnalysis() {
		return regimeAnalysis;
	}

	/**
	 * Returns regime edit position.
	 * @return regime
	 */
	public RegimeEditPosition getRegimeEditPosition() {
		return regimeEditPosition;
	}
	
	/**
	 * Returns regime ending training.
	 * @return regime
	 */
	public RegimeEndingTraining getRegimeEndingTraining() {
		return regimeEndingTraining;
	}

	/**
	 * Returns registrar of application listeners.
	 * @return registrar of application listeners
	 */
	public HandlerRegistrarImpl<IApplicationListener> getApplicationListenerRegistrar() {
		return applicationListenerRegistrar;
	}

	/**
	 * Changes regime of the application.
	 * @param regimeType new regime type
	 */
	public void setRegimeType (final RegimeType regimeType) {
		if (actualRegime != null) {
			actualRegime.deactivateRegime();
		}
		
		switch (regimeType) {
			case PLAY:
				actualRegime = regimePlay;
				break;

			case EDIT_POSITION:
				actualRegime = regimeEditPosition;
				break;
				
			case ANALYSIS:
				actualRegime = regimeAnalysis;
				break;
				
			case ENDING_TRAINING:
				actualRegime = regimeEndingTraining;
				break;
		}
		
		actualRegime.activateRegime();
		
		for (IApplicationListener listener: applicationListenerRegistrar.getHandlers()) {
			listener.onRegimeChanged();
		}
	}

	/**
	 * Returns all settings.
	 * @return all settings
	 */
	public ApplicationSettings getSettings() {
		return settings;
	}
	
	/**
	 * Returns localization instance.
	 * @return localization
	 */
	public ILocalization getLocalization() {
		return localization;
	}
	
	private void loadSettings() {
		settings.setDefaults();
		
		try {
			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document document = builder.parse(SETTINGS_PATH);
			
			settings.readFromXmlElement(document.getDocumentElement());
			updateSettings();
		}
		catch (Throwable th) {
			settings.setDefaults();
			Logger.logException(th);
		}
	}
	
	private void saveSettings() {
		try {
			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document document = builder.newDocument();
			
			final Element rootElement = document.createElement(SETTINGS_ELEMENT);
			document.appendChild(rootElement);
			
			settings.writeToXmlElement(rootElement);
			
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			final Source source = new DOMSource(document);
			final StreamResult streamResult = new StreamResult(SETTINGS_PATH);
			
			transformer.transform (source, streamResult);
		}
		catch (Throwable th) {
			Logger.logException(th);
		}
	}
	
	/**
	 * Returns localized component register.
	 * @return localized component register
	 */
	public ILocalizedComponentRegister getLocalizedComponentRegister() {
		return localizedComponentRegister;
	}
	
	/**
	 * Returns resources to the search.
	 * @return search resources
	 */
	public SearchResources getSearchResources() {
		return searchResources;
	}

	public void setSearchResources(final SearchResources searchResources) {
		this.searchResources = searchResources;
	}
	
	/**
	 * Updates application according to settings.
	 */
	public void updateSettings() {
		try {
			final String selectedLanguage = settings.getGuiSettings().getLanguage();
				
			if (!localization.getLanguage().equals(selectedLanguage)) {
				localization.setLanguage (selectedLanguage);
				localizedComponentRegister.updateLanguage();
			}
			
			if (actualRegime != null) {
				actualRegime.deactivateRegime();
				actualRegime.activateRegime();
			}
		}
		catch (IOException ex) {
			throw new RuntimeException("Cannot update settings", ex);
		}
	}
	
	/**
	 * This method is called when application is closed.
	 */
	public void onClose() {
		saveSettings();
		
		regimePlay.destroy();
		regimeEditPosition.destroy();
		regimeAnalysis.destroy();
		
		if (GlobalSettings.isDebug()) {
			localizedComponentRegister.printRegisteredComponents(System.err, "Still registered components:");
		}
	}
	
	@Override
	public void setGameRegime() {
		final GameSettings gameSettings = settings.getGameSettings();
		
		switch (gameSettings.getGameType()) {
			case PLAY:
				setRegimeType(RegimeType.PLAY);
				break;
				
			case ANALYSIS:
				setRegimeType(RegimeType.ANALYSIS);
				break;
				
			case ENDING_TRAINING:
				setRegimeType(RegimeType.ENDING_TRAINING);
				break;
		}
	}

	private String getPlayerName (final int color) {
		final GameSettings gameSettings = settings.getGameSettings();
		final SideSettings sideSettings = gameSettings.getSideSettings(color);
		
		switch (sideSettings.getSideType()) {
			case HUMAN:
				return "Player";
				
			case COMPUTER:
				return "Bishop " + (sideSettings.getTimeForMove() / 1000) + "s per move";
				
			default:
				return "";
		}
	}

	@Override
	public void newGame() {
		startGame(Position.INITIAL_POSITION);
	}


	private void startGame(final Position position) {
		gameEditor.newGame(position);
		setGameRegime();
		
		final GameSettings gameSettings = settings.getGameSettings();
		final GameHeader header = gameEditor.getGame().getHeader();
		
		if (gameSettings.getGameType() == GameType.PLAY) {
			header.setWhite(getPlayerName(Color.WHITE));
			header.setBlack(getPlayerName(Color.BLACK));
		}
	}
	
	@Override
	public void endingTraining (final MaterialHash materialHash, final int difficulty) {
		final Position position = findPositionWithDifficulty(materialHash, difficulty);
		
		if (position != null) {
			final int playerColor = materialHash.getOnTurn();
			final int computerColor = Color.getOppositeColor(playerColor);
			
			final GameSettings gameSettings = settings.getGameSettings();
			
			final SideSettings playerSide = gameSettings.getSideSettings(playerColor);
			playerSide.setSideType(SideType.HUMAN);
	
			final SideSettings computerSide = gameSettings.getSideSettings(computerColor);
			computerSide.setSideType(SideType.COMPUTER);
			computerSide.setTimeForMove(1000);
	
			gameSettings.setGameType(GameType.ENDING_TRAINING);
			setGameRegime();
			
			startGame (position);
		}
	}

	private Position findPositionWithDifficulty(final MaterialHash materialHash, final int difficulty) {
		final Map<Integer, Position> resultMap = calculateResultMap(materialHash);
		final Position selectedResult = selectResult(resultMap, difficulty);
		
		// TODO symmetry
		return selectedResult;
	}

	private Position selectResult(final Map<Integer, Position> resultMap, final int difficulty) {
		if (resultMap.isEmpty())
			return null;
		
		final int min = Collections.min(resultMap.keySet());
		final int max = Collections.max(resultMap.keySet());
		final int optimal = min + (max - min) * (difficulty - IApplication.MIN_DIFFICULTY) / (IApplication.MAX_DIFFICULTY - IApplication.MIN_DIFFICULTY);
		
		int bestDepth = -1;
		
		for (int depth: resultMap.keySet()) {
			if (bestDepth < 0 || Math.abs(depth - optimal) < Math.abs(bestDepth - optimal))
				bestDepth = optimal;
		}
		
		return resultMap.get(bestDepth);
	}


	private Map<Integer, Position> calculateResultMap(
			final MaterialHash materialHash) {
		final TablebasePositionEvaluator tableEvaluator = searchResources.getTablebasePositionEvaluator();
		final ITableRead table = tableEvaluator.getTable (materialHash);
		final TableDefinition definition = table.getDefinition();
		final long itemCount = definition.getTableIndexCount();
		
		final Random rnd = new Random();
		final long num = rnd.nextLong();
		final long absNum = num & ~(1L << (Long.SIZE-1));
		final long begin = absNum % itemCount;
		
		ITableIteratorRead it = table.getIterator();
		it.moveForward(begin);
		
		final int MAX_COUNT_WITH_WIN = 10000;
		final Map<Integer, Position> resultMap = new TreeMap<Integer, Position>();
		
		for (long i = 0; i < itemCount; i++) {
			final int result = it.getResult();
			
			if (TableResult.isWin(result)) {
				final int depth = TableResult.getWinDepth(result);
				
				if (!resultMap.containsKey(depth)) {
					final Position position = new Position();
					it.fillPosition(position);
					
					resultMap.put(depth, position);
				}
				
				if (i >= MAX_COUNT_WITH_WIN)
					break;
			}
			
			it.next();
			
			if (!it.isValid())
				it = table.getIterator();
		}
		return resultMap;
	}
}
