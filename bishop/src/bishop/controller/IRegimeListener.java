package bishop.controller;

public interface IRegimeListener {
	/**
	 * Method is called when regime is activated.
	 */
	public void onRegimeActivated();
	
	/**
	 * Method is called when regime is deactivated.
	 */
	public void onRegimeDeactivated();
}
