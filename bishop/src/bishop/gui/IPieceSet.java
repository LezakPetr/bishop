package bishop.gui;

import math.IVector;

public interface IPieceSet {
	/**
	 * Returns name of this piece set.
	 * @return name of this piece set
	 */
	public String getName();
	
	/**
	 * Returns center point of the pieces.
	 * @return center point of the pieces
	 */
	public IVector getCenterPoint();

	/**
	 * Returns size of pieces.
	 * @return size of piece images
	 */
	public IVector getPieceSize();
	
	/**
	 * Returns piece set scaled by given ratio.
	 * @param scale scale ratio
	 * @return scaled board
	 */
	public RasterPieceSet renderScaledPieceSet (final double scale);
}
