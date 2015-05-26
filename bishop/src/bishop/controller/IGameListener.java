package bishop.controller;

public interface IGameListener {
	/**
	 * This method is called when position is changed.
	 */
	public void onActualPositionChanged();
	
	/**
	 * This method is called when game is changed.
	 */
	public void onGameChanged();
	
	/**
	 * This method is called when move is done in the position.
	 */
	public void onMove();
}
