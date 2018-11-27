package bishop.controller;

import java.net.URL;

import bishop.base.HandlerRegistrarImpl;
import bishop.base.MaterialHash;

public interface IApplication {
	
	public static final int MIN_DIFFICULTY = 0;
	public static final int MAX_DIFFICULTY = 100;
	
	/**
	 * Returns registrar of application listeners.
	 * @return registrar of application listeners
	 */
	public HandlerRegistrarImpl<IApplicationListener> getApplicationListenerRegistrar();
	
	/**
	 * Returns editor of actual game.
	 * @return game editor
	 */
	public GameEditor getActualGameEditor();
	
	/**
	 * Returns type of actual regime.
	 * @return regime type
	 */
	public RegimeType getRegimeType();
	
	/**
	 * Changes regime of the application.
	 * @param regimeType new regime type
	 */
	public void setRegimeType (final RegimeType regimeType);

	/**
	 * Changes regime type to previous one.
	 */
	public void popRegimeType();
	
	/**
	 * Returns regime play.
	 * @return regime
	 */
	public RegimePlay getRegimePlay();

	/**
	 * Returns regime analysis.
	 * @return regime
	 */
	public RegimeAnalysis getRegimeAnalysis();

	/**
	 * Returns regime edit position.
	 * @return regime
	 */
	public RegimeEditPosition getRegimeEditPosition();
	
	/**
	 * Returns regime ending training.
	 * @return regime
	 */
	public RegimeEndingTraining getRegimeEndingTraining();

	/**
	 * Returns all settings.
	 * @return all settings
	 */
	public ApplicationSettings getSettings();
	
	/**
	 * Updates application according to settings.
	 */
	public void updateSettings();
	
	/**
	 * Returns localization instance.
	 * @return localization
	 */
	public ILocalization getLocalization();
	
	/**
	 * Returns localized component register.
	 * @return localized component register
	 */
	public ILocalizedComponentRegister getLocalizedComponentRegister();
	
	/**
	 * This method is called when application is closed.
	 */
	public void onClose();
	
	/**
	 * Returns root URL of the application.
	 * @return URL
	 */
	public URL getRootUrl();
	
	/**
	 * Returns resources to the search.
	 * @return search resources
	 */
	public SearchResources getSearchResources();

	/**
	 * Updates game regime by game settings.
	 */
	public void setGameRegime();
	
	/**
	 * Starts new game.
	 */
	public void newGame();
	
	/**
	 * Starts ending training.
	 * @param materialHash material hash
	 * @param difficulty difficulty of the position
	 */
	public void endingTraining (final MaterialHash materialHash, final int difficulty);
}
