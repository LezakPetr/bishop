package bishop.base;

public interface IPieceCounts {
	/**
	 * Returns number of pieces with given color and type.
	 * @param color color of piece
	 * @param pieceType type of piece
	 * @return number of pieces
	 */
	public int getPieceCount (final int color, final int pieceType);

	/**
	 * Returns difference between white and black number of pieces of given type.
	 * @param pieceType piece type
	 * @return piece count difference
	 */
	public default int getPieceCountDiff (final int pieceType) {
		return getPieceCount(Color.WHITE, pieceType) - getPieceCount(Color.BLACK, pieceType);
	}
}
