package bishop.base;

import java.util.stream.IntStream;

public class MaterialHashConstants {
	public static final long HASH_ALONE_KINGS = 0;
	
	public static final int BITS_PER_ITEM = Square.BIT_COUNT;   // Number of bits for the count of pieces
	public static final long ITEM_MASK = (1L << BITS_PER_ITEM) - 1;
	
	public static final int ON_TURN_OFFSET = BITS_PER_ITEM * Color.LAST * PieceType.VARIABLE_COUNT;
	public static final long ON_TURN_MASK = 1L << ON_TURN_OFFSET;
	
	// 000T pppp ppPP PPPP nnnn nnNN NNNN bbbb bbBB BBBB rrrr rrRR RRRR qqqq qqQQ QQQQ

	private static final int[][] PIECE_OFFSETS = new int[Color.LAST][PieceType.LAST];
	private static final long[][] PIECE_MASKS = new long[Color.LAST][PieceType.LAST];
	private static final long[][] PIECE_INCREMENT = new long[Color.LAST][PieceType.LAST];
	private static final long[] COLOR_MASKS = new long[Color.LAST];
	private static final long[] QUEEN_ROOK_OR_PAWN_MASKS = new long[Color.LAST];
	private static final long[] FIGURE_MASKS = new long[Color.LAST];
	
	static {
		int offset = 0;
		
		for (int pieceType = PieceType.VARIABLE_FIRST; pieceType < PieceType.VARIABLE_LAST; pieceType++) {
			for (int color = Color.FIRST; color < Color.LAST; color++) {
				PIECE_OFFSETS[color][pieceType] = offset;
				PIECE_MASKS[color][pieceType] = ITEM_MASK << offset;
				PIECE_INCREMENT[color][pieceType] = 1L << offset;
				
				offset += BITS_PER_ITEM;
			}
		}

		final int[] figures = IntStream.range(PieceType.PROMOTION_FIGURE_FIRST, PieceType.PROMOTION_FIGURE_LAST).toArray();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			COLOR_MASKS[color] = combineMasks(PIECE_MASKS[color]);
			QUEEN_ROOK_OR_PAWN_MASKS[color] = calculateMaskForPieces(color, PieceType.QUEEN, PieceType.ROOK, PieceType.PAWN);
			FIGURE_MASKS[color] = calculateMaskForPieces(color, figures);
		}
	}

	public static final long WHITE_COLOR_MASK = getColorMask(Color.WHITE);
	public static final long BLACK_COLOR_MASK = getColorMask(Color.BLACK);

	public static final long QUEEN_ROOK_OR_PAWN_BOTH_COLOR_MASK = combineMasks(QUEEN_ROOK_OR_PAWN_MASKS);
	public static final long FIGURE_BOTH_COLOR_MASK = combineMasks(FIGURE_MASKS);

	public static final long TOTAL_PIECE_COUNT_MULTIPLICAOR = 0x0410410410410410L;
	public static final int TOTAL_PIECE_COUNT_SHIFT = 58;
	
	/**
	 * Returns offset of the count of given pieces. 
	 * @param color color of the piece
	 * @param pieceType type of the piece
	 * @return offset
	 */
	public static int getOffset(final int color, final int pieceType) {
		return PIECE_OFFSETS[color][pieceType];
	}

	/**
	 * Returns mask of the count of given pieces. 
	 * @param color color of the piece
	 * @param pieceType type of the piece
	 * @return mask
	 */
	public static long getPieceMask (final int color, final int pieceType) {
		return PIECE_MASKS[color][pieceType];
	}

	/**
	 * Returns increment of the count of given pieces. 
	 * @param color color of the piece
	 * @param pieceType type of the piece
	 * @return 1L << getOffset(color, pieceType)
	 */
	public static long getPieceIncrement (final int color, final int pieceType) {
		return PIECE_INCREMENT[color][pieceType];
	}

	/**
	 * Returns mask of counts of all pieces with given color
	 * @param color color
	 * @return mask
	 */
	public static long getColorMask (final int color) {
		return COLOR_MASKS[color];
	}

	/**
	 * Returns mask of counts of queens, rooks and pawns with given color
	 * @param color color
	 * @return mask
	 */
	public static long getQueenRookOrPawnOnSide(final int color) {
		return QUEEN_ROOK_OR_PAWN_MASKS[color];
	}
	
	/**
	 * Returns mask of counts of given pieces with given color
	 * @param color color
	 * @param pieceTypes types of pieces
	 * @return mask
	 */
	public static long calculateMaskForPieces (final int color, final int... pieceTypes) {
		long mask = 0;
		
		for (int pieceType: pieceTypes)
			mask |= getPieceMask(color, pieceType);
		
		return mask;
	}

	private static long combineMasks (final long...masks) {
		long result = 0;
		
		for (long mask: masks)
			result |= mask;
		
		return result;
	}
}
