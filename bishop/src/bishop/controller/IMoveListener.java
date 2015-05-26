package bishop.controller;

import bishop.base.Move;

public interface IMoveListener {

	/**
	 * Method is called when move is entered.
	 * @param move entered move
	 */
	public void onMove (final Move move);
	
}
