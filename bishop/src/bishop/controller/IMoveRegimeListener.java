package bishop.controller;

public interface IMoveRegimeListener extends IRegimeListener {
	/**
	 * Method is called when game position is changed. 
	 */
	public void onGamePositionChanged();
}
