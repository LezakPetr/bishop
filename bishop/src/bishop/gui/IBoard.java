package bishop.gui;

import math.IVectorRead;

public interface IBoard {
	/**
	 * Returns name of the board.
	 * @return name of the board
	 */
	public String getName();

	/**
	 * Sets name of the board.
	 * @param name name of the board
	 */
	public void setName(final String name);

	/**
	 * Returns position of corner with lower coordinates.
	 * @return position
	 */
	public IVectorRead getMinSquareCorner();

	/**
	 * Returns position of corner with upper coordinates.
	 * @return position
	 */
	public IVectorRead getMaxSquareCorner();
	
	/**
	 * Returns position of board center.
	 * @return position
	 */	
	public IVectorRead getBoardCenterPoint();
	
	/**
	 * Returns board scaled by given ratio.
	 * @param scale scale ratio
	 * @return scaled board
	 */
	public RasterBoard renderScaledBoard (final double scale);
}
