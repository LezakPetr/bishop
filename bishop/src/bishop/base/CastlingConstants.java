package bishop.base;

public class CastlingConstants {
	// Mask of squares between king and rook before castling.
	private static final long[] TABLE_CASTLING_MIDDLE_SQUARE_MASK = {
			// White
			BitBoard.getSquareMask(Square.F1) | BitBoard.getSquareMask(Square.G1),
			BitBoard.getSquareMask(Square.B1) | BitBoard.getSquareMask(Square.C1) | BitBoard.getSquareMask(Square.D1),

			// Black
			BitBoard.getSquareMask(Square.F8) | BitBoard.getSquareMask(Square.G8),
			BitBoard.getSquareMask(Square.B8) | BitBoard.getSquareMask(Square.C8) | BitBoard.getSquareMask(Square.D8)};

	// Begin squares of rook in castling.
	private static final int[] TABLE_CASTLING_ROOK_BEGIN_SQUARE = {Square.H1, Square.A1, Square.H8, Square.A8};

	// Begin squares of king in the castling.
	private static final int[] TABLE_CASTLING_KING_BEGIN_SQUARE = {Square.E1, Square.E8};

	// Target squares of king in castling.
	private static final int[] TABLE_CASTLING_KING_TARGET_SQUARE = {Square.G1, Square.C1, Square.G8, Square.C8};

	// Middle squares of king in castling.
	private static final int[] TABLE_CASTLING_KING_MIDDLE_SQUARE = {Square.F1, Square.D1, Square.F8, Square.D8};

	// Contains changes of king mask for given type of castling.
	private static final long[] TABLE_CASTLING_KING_CHANGE_MASK = {
			BitBoard.getSquareMask(Square.E1) | BitBoard.getSquareMask(Square.G1),
			BitBoard.getSquareMask(Square.E1) | BitBoard.getSquareMask(Square.C1),

			BitBoard.getSquareMask(Square.E8) | BitBoard.getSquareMask(Square.G8),
			BitBoard.getSquareMask(Square.E8) | BitBoard.getSquareMask(Square.C8)
	};

	// Contains changes of king mask for given type of castling.
	private static final long[] TABLE_CASTLING_ROOK_CHANGE_MASK = {
			BitBoard.getSquareMask(Square.H1) | BitBoard.getSquareMask(Square.F1),
			BitBoard.getSquareMask(Square.A1) | BitBoard.getSquareMask(Square.D1),

			BitBoard.getSquareMask(Square.H8) | BitBoard.getSquareMask(Square.F8),
			BitBoard.getSquareMask(Square.A8) | BitBoard.getSquareMask(Square.D8)
	};

	/**
	 * Returns mask of squares between king and rook before castling.
	 *
	 * @param color        color of player
	 * @param castlingType type of castling
	 * @return required mask
	 */
	public static long getCastlingMiddleSquareMask(final int color, final int castlingType) {
		final int index = (color << CastlingType.BIT_COUNT) + castlingType;

		return TABLE_CASTLING_MIDDLE_SQUARE_MASK[index];
	}

	/**
	 * Return begin square of rook in castling.
	 *
	 * @param color        color of player
	 * @param castlingType type of castling
	 * @return begin square of rook
	 */
	public static int getCastlingRookBeginSquare(final int color, final int castlingType) {
		final int index = (color << CastlingType.BIT_COUNT) + castlingType;

		return TABLE_CASTLING_ROOK_BEGIN_SQUARE[index];
	}

	/**
	 * Return target square of rook in castling.
	 *
	 * @param color        color of player
	 * @param castlingType type of castling
	 * @return target square of rook
	 */
	public static int getCastlingRookTargetSquare(final int color, final int castlingType) {
		final int index = (color << CastlingType.BIT_COUNT) + castlingType;

		return TABLE_CASTLING_KING_MIDDLE_SQUARE[index];   // Same as rook target square
	}

	public static int getCastlingKingBeginSquare(final int color) {
		return TABLE_CASTLING_KING_BEGIN_SQUARE[color];
	}

	/**
	 * Return target square of king in castling.
	 *
	 * @param color        color of player
	 * @param castlingType type of castling
	 * @return target square of king
	 */
	public static int getCastlingKingTargetSquare(final int color, final int castlingType) {
		final int index = (color << CastlingType.BIT_COUNT) + castlingType;

		return TABLE_CASTLING_KING_TARGET_SQUARE[index];
	}

	/**
	 * Return target square of king in castling.
	 *
	 * @param color        color of player
	 * @param castlingType type of castling
	 * @return target square of king
	 */
	public static int getCastlingKingMiddleSquare(final int color, final int castlingType) {
		final int index = (color << CastlingType.BIT_COUNT) + castlingType;

		return TABLE_CASTLING_KING_MIDDLE_SQUARE[index];
	}

	/**
	 * Returns changes of king mask for given type of castling.
	 *
	 * @param color        color of player
	 * @param castlingType type of castling
	 * @return required mask
	 */
	public static long getCastlingKingChangeMask(final int color, final int castlingType) {
		final int index = (color << CastlingType.BIT_COUNT) + castlingType;

		return TABLE_CASTLING_KING_CHANGE_MASK[index];
	}

	/**
	 * Returns changes of king mask for given type of castling.
	 *
	 * @param color        color of player
	 * @param castlingType type of castling
	 * @return required mask
	 */
	public static long getCastlingRookChangeMask(final int color, final int castlingType) {
		final int index = (color << CastlingType.BIT_COUNT) + castlingType;

		return TABLE_CASTLING_ROOK_CHANGE_MASK[index];
	}
}
