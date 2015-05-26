package bishop.gui;

import bishop.base.IHandlerRegistrar;
import bishop.base.Position;

public interface IDesk {
	/**
	 * Returns desk listener registrar.
	 * @return desk listener registrar
	 */
	public IHandlerRegistrar<IDeskListener> getDeskListenerRegistrar();
	
	/**
	 * Changes position to given one.
	 * Position is copied.
	 * @param position new position
	 */
	public void changePosition (final Position position);
	
	/**
	 * Returns position on the desk.
	 * Position cannot be changed.
	 * @return position
	 */
	public Position getPosition();
	
	/**
	 * Changes mask of marked squares.
	 * @param markedSquareMask new mask of marked squares
	 */
	public void changeMarkedSquares (final long markedSquareMask);
	
	/**
	 * Selects type of promotion piece.
	 * @param color color of piece
	 * @return type of promotion piece
	 */
	public int selectPromotionPieceType (final int color);

	/**
	 * Starts dragging from given square.
	 * @param square begin square of the dragging
	 */
	public void startDragging (final int square);
	
	/**
	 * Stops dragging.
	 */
	public void stopDragging();
}
