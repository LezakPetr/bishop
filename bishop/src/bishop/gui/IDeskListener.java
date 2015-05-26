package bishop.gui;

import bisGui.widgets.MouseButton;

public interface IDeskListener {
	/**
	 * This method is called when user clicks on some square.
	 * @param square coordinate of square
	 * @param button clicked button
	 */
	public void onSquareClick (final int square, final MouseButton button);

	/**
	 * This method is called when user starts dragging from some square.
	 * @param square source square
	 * @param button dragging button
	 */
	public void onDrag (final int square, final MouseButton button);
	
	/**
	 * This method is called when user drops object to some square.
	 * @param beginSquare begin square of the dragging
	 * @param targetSquare target square of the dragging
	 */
	public void onDrop (final int beginSquare, final int targetSquare);
}
