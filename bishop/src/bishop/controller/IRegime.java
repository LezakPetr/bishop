package bishop.controller;


public interface IRegime {
	/**
	 * Activates this regime.
	 */
	public void activateRegime();

	/**
	 * Deactivates this regime.
	 */
	public void deactivateRegime();

	/**
	 * Removes all handlers from the application and frees resources.
	 */
	public void destroy();
	
	/**
	 * Returns type of regime.
	 * @return regime type
	 */
	public RegimeType getRegimeType();
}
