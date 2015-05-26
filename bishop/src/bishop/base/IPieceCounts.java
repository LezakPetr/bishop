package bishop.base;

public interface IPieceCounts {
	/**
	 * Returns number of pieces with given color and type.
	 * @param color color of piece
	 * @param pieceType type of piece
	 * @return number of pieces
	 */
	public int getPieceCount (final int color, final int pieceType);
}
