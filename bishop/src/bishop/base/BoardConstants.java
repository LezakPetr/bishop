package bishop.base;

import bishop.tables.FigureAttackTable;
import utils.IntBiPredicate;
import utils.LongArrayBuilder;

/**
 * This class contains some constants related to the board.
 * 
 * @author Ing. Petr Ležák
 */
public class BoardConstants {
	// Mask of ranks
	public static final long RANK_1_MASK = 0x00000000000000FFL;
	public static final long RANK_2_MASK = 0x000000000000FF00L;
	public static final long RANK_3_MASK = 0x0000000000FF0000L;
	public static final long RANK_4_MASK = 0x00000000FF000000L;
	public static final long RANK_5_MASK = 0x000000FF00000000L;
	public static final long RANK_6_MASK = 0x0000FF0000000000L;
	public static final long RANK_7_MASK = 0x00FF000000000000L;
	public static final long RANK_8_MASK = 0xFF00000000000000L;

	public static final long RANK_18_MASK = RANK_1_MASK | RANK_8_MASK;
	public static final long PAWN_ALLOWED_SQUARES = ~BoardConstants.RANK_18_MASK;

	// Mask of files
	public static final long FILE_A_MASK = 0x0101010101010101L;
	public static final long FILE_B_MASK = 0x0202020202020202L;
	public static final long FILE_C_MASK = 0x0404040404040404L;
	public static final long FILE_D_MASK = 0x0808080808080808L;
	public static final long FILE_E_MASK = 0x1010101010101010L;
	public static final long FILE_F_MASK = 0x2020202020202020L;
	public static final long FILE_G_MASK = 0x4040404040404040L;
	public static final long FILE_H_MASK = 0x8080808080808080L;

	public static final long FILE_ACFH_MASK = FILE_A_MASK | FILE_C_MASK | FILE_F_MASK | FILE_H_MASK;

	public static final long WHITE_SQUARE_MASK = 0x55AA55AA55AA55AAL;
	public static final long BLACK_SQUARE_MASK = 0xAA55AA55AA55AA55L;

	public static final long BOARD_EDGE_MASK = FILE_A_MASK | FILE_H_MASK | RANK_1_MASK | RANK_8_MASK;

	public static final long RANK_1278_MASK = RANK_1_MASK | RANK_2_MASK | RANK_7_MASK | RANK_8_MASK;
	public static final long RANK_123678_MASK = RANK_1_MASK | RANK_2_MASK | RANK_3_MASK | RANK_6_MASK | RANK_7_MASK | RANK_8_MASK;

	private static final long[] FIRST_RANK_MASKS = LongArrayBuilder.create(Color.LAST)
			.put(Color.WHITE, RANK_1_MASK)
			.put(Color.BLACK, RANK_8_MASK)
			.build();

	private static final long[] SECOND_RANK_MASKS = LongArrayBuilder.create(Color.LAST)
			.put(Color.WHITE, RANK_2_MASK)
			.put(Color.BLACK, RANK_7_MASK)
			.build();

	// Mask of squares where pawn can capture to the left and to the right.
	// First and eight rank must be preserved to be able to calculate reverse
	// attacks.
	public static final long LEFT_PAWN_CAPTURE_MASK = ~FILE_A_MASK;
	public static final long RIGHT_PAWN_CAPTURE_MASK = ~FILE_H_MASK;

	private static final long[] TABLE_EP_RANK_MASKS = initializeTableEpRankMasks();

	private static long[] initializeTableEpRankMasks() {
		final long[] table = new long[Color.LAST];

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int rank = getEpRank(color);
			long mask = 0;

			for (int file = File.FIRST; file < File.LAST; file++) {
				mask |= BitBoard.getSquareMask(Square.onFileRank(file, rank));
			}

			table[color] = mask;
		}

		return table;
	}

	// Distances of pawns with given color on given ranks from promotion
	private static final int[] PAWN_PROMOTION_DISTANCES = {-1, 5, 5, 4, 3, 2, 1, 0, // White
			0, 1, 2, 3, 4, 5, 5, -1 // Black
	};

	private static long[][] initializeSquareMaskBySquareTableBothColors(final IntBiPredicate predicate) {
		final long[][] result = new long[Color.LAST][Square.LAST];

		for (int drivingSquare = Square.FIRST; drivingSquare < Square.LAST; drivingSquare++) {
			long mask = BitBoard.EMPTY;

			for (int maskedSquare = Square.FIRST; maskedSquare < Square.LAST; maskedSquare++) {
				if (predicate.test(drivingSquare, maskedSquare))
					mask |= BitBoard.getSquareMask(maskedSquare);
			}

			result[Color.WHITE][drivingSquare] = mask;
			result[Color.BLACK][Square.getOppositeSquare(drivingSquare)] = BitBoard.getMirrorBoard(mask);
		}

		return result;
	}

	private static long[] initializeSquareMaskBySquareTableSingleColor(final IntBiPredicate predicate) {
		final long[] result = new long[Square.LAST];

		for (int drivingSquare = Square.FIRST; drivingSquare < Square.LAST; drivingSquare++) {
			long mask = BitBoard.EMPTY;

			for (int maskedSquare = Square.FIRST; maskedSquare < Square.LAST; maskedSquare++) {
				if (predicate.test(drivingSquare, maskedSquare))
					mask |= BitBoard.getSquareMask(maskedSquare);
			}

			result[drivingSquare] = mask;
		}

		return result;
	}


	private static final long[][] FRONT_SQUARES_ON_THREE_FILES = initializeSquareMaskBySquareTableBothColors(
			(d, m) -> Square.getRank(m) > Square.getRank(d) && Math.abs(Square.getFile(m) - Square.getFile(d)) <= 1);

	private static final long[][] FRONT_SQUARES_ON_NEIGHBOR_FILES = initializeSquareMaskBySquareTableBothColors(
			(d, m) -> Square.getRank(m) > Square.getRank(d) && Math.abs(Square.getFile(m) - Square.getFile(d)) == 1);

	private static final long[][] PAWN_BLOCKING_SQUARES = initializeSquareMaskBySquareTableBothColors((d, m) -> {
		final int dRank = Square.getRank(d);
		final int mRank = Square.getRank(m);
		final int dFile = Square.getFile(d);
		final int mFile = Square.getFile(m);

		return (dFile == mFile && mRank > dRank) || (Math.abs(dFile - mFile) == 1 && mRank - dRank >= 2);
	});

	private static final long[][] SQUARES_IN_FRONT_INCLUSIVE = initializeSquareMaskBySquareTableBothColors(
			(d, m) -> Square.getFile(d) == Square.getFile(m) && Square.getRank(m) >= Square.getRank(d));

	private static final long[][] SQUARES_IN_FRONT_EXCLUSIVE = initializeSquareMaskBySquareTableBothColors(
			(d, m) -> Square.getFile(d) == Square.getFile(m) && Square.getRank(m) > Square.getRank(d));

	private static final long[] CONNECTED_PAWN_SQUARE_MASKS = initializeConnectedPawnSquareMasks();

	private static final long[] KING_SAFETY_FAR_SQUARES = initializeSquareMaskBySquareTableSingleColor((d, m) -> {
		final int dRank = Square.getRank(d);
		final int mRank = Square.getRank(m);
		final int dFile = Square.getFile(d);
		final int mFile = Square.getFile(m);

		return Math.abs(dFile - mFile) <= 1 && Math.abs(dRank - mRank) <= 2;
	});

	private static long[] initializeConnectedPawnSquareMasks() {
		final long[] table = new long[Square.LAST];

		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final int file = Square.getFile(square);
			final int rank = Square.getRank(square);

			long board = BitBoard.EMPTY;

			if (file > File.FA) {
				final int neighborSquare = Square.onFileRank(file - 1, rank);
				board |= BitBoard.getSquareMask(neighborSquare);
			}

			if (file < File.FH) {
				final int neighborSquare = Square.onFileRank(file + 1, rank);
				board |= BitBoard.getSquareMask(neighborSquare);
			}

			table[square] = board;
		}

		return table;
	}

	private static final long[][] PREV_EP_FILE_MASK = initializePrevEpFileMask();

	private static long[][] initializePrevEpFileMask() {
		final long[][] table = new long[Color.LAST][File.LAST];

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int rank = getEpRank(color);

			for (int file = File.FIRST; file < File.LAST; file++) {
				long board = BitBoard.EMPTY;

				if (file > File.FA) {
					final int prevSquare = Square.onFileRank(file - 1, rank);
					board |= BitBoard.getSquareMask(prevSquare);
				}

				table[color][file] = board;
			}
		}

		return table;
	}

	private static final long[][] NEXT_EP_FILE_MASK = initializeNextEpFileMask();

	private static long[][] initializeNextEpFileMask() {
		final long[][] table = new long[Color.LAST][File.LAST];

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int rank = getEpRank(color);

			for (int file = File.FIRST; file < File.LAST; file++) {
				long board = BitBoard.EMPTY;

				if (file < File.FH) {
					final int nextSquare = Square.onFileRank(file + 1, rank);
					board |= BitBoard.getSquareMask(nextSquare);
				}

				table[color][file] = board;
			}
		}

		return table;
	}

	public static long getSquareColorMask(final int squareColor) {
		return WHITE_SQUARE_MASK ^ (-(long) squareColor);
	}

	public static long getRankMask(final int rank) {
		return 0xFFL << (File.LAST * rank);
	}

	public static long getFileMask(final int file) {
		return 0x0101010101010101L << file;
	}

	/**
	 * Returns rank where pawns with given color moves by two squares.
	 *
	 * @param color pawn color
	 * @return Rank.R4 for white or Rank.R5 for black
	 */
	public static int getEpRank(final int color) {
		return Rank.R4 + color;
	}

	/**
	 * Returns square where pawn with given color moves by two squares.
	 *
	 * @param color pawn color
	 * @param file  file where is the pawn
	 * @return EP square
	 */
	public static int getEpSquare(final int color, final int file) {
		return Square.A4 + (color << File.BIT_COUNT) + file;
	}

	/**
	 * Returns target square of the capturing pawn.
	 *
	 * @param color  color of the captured (opposite to capturing) pawn
	 * @param epFile file where the pawn has moved by 2 squares
	 * @return EP target square
	 */
	public static int getEpTargetSquare(final int color, final int epFile) {
		final int colorComponent = (-color) & 24;   // White = 0; Black = 24

		return Square.A3 + colorComponent + epFile;
	}

	public static long getEpRankMask(final int color) {
		return TABLE_EP_RANK_MASKS[color];
	}

	public static int getPawnInitialSquare(final int color, final int file) {
		return Square.onFileRank(file, Rank.getAbsolute(Rank.R2, color));
	}

	private static final byte[] KING_SQUARE_DISTANCES = initializeKingSquareDistances();

	private static byte[] initializeKingSquareDistances() {
		final byte[] table = new byte[Square.LAST * Square.LAST];

		for (int beginSquare = Square.FIRST; beginSquare < Square.LAST; beginSquare++) {
			for (int endSquare = Square.FIRST; endSquare < Square.LAST; endSquare++) {
				final int beginFile = Square.getFile(beginSquare);
				final int beginRank = Square.getRank(beginSquare);
				final int endFile = Square.getFile(endSquare);
				final int endRank = Square.getRank(endSquare);

				final int index = getTwoSquaresIndex(beginSquare, endSquare);
				table[index] = (byte) Math.max(Math.abs(beginFile - endFile), Math.abs(beginRank - endRank));
			}
		}

		return table;
	}

	public static int getKingSquareDistance(final int beginSquare, final int endSquare) {
		final int index = getTwoSquaresIndex(beginSquare, endSquare);

		return KING_SQUARE_DISTANCES[index];
	}

	private static int getTwoSquaresIndex(int beginSquare, int endSquare) {
		return (beginSquare << Square.BIT_COUNT) + endSquare;
	}

	/**
	 * Returns promotion rank for given pawn color.
	 *
	 * @param color pawn color
	 * @return promotion rank
	 */
	public static int getPawnPromotionRank(final int color) {
		return (color - 1) & 0x07;
	}

	public static int getPawnPromotionSquare(final int color, final int pawnSquare) {
		// Rank part of promotion square.
		//   0x38 ( = Rank.R8 << File.BIT_COUNT) for white
		//   0x00 ( = Rank.R1 << File.BIT_COUNT) for black
		final int rankPart = (color - 1) & 0x38;

		// File part of promotion square
		final int filePart = pawnSquare & 0x07;

		return rankPart | filePart;
	}

	public static int getPawnPromotionDistance(final int color, final int pawnSquare) {
		final int pawnRank = Square.getRank(pawnSquare);
		final int index = (color << Rank.BIT_COUNT) + pawnRank;

		return PAWN_PROMOTION_DISTANCES[index];
	}

	/**
	 * Returns offset of rank for move of pawn with given color.
	 * @param color color of the pawn
	 * @return the offset (+1 for white, -1 for black)
	 */
	public static int getPawnRankOffset(final int color) {
		return 1 - (color << 1);
	}

	/**
	 * Returns offset of square for move of pawn with given color.
	 * @param color color of the pawn
	 * @return the offset
	 */
	public static int getPawnSquareOffset(final int color) {
		return 8 - (color << 4);
	}

	/**
	 * Returns mask of squares in front of given square on same and neighbor
	 * files.
	 *
	 * @param color  color of player
	 * @param square square
	 * @return mask of squares in front of given square
	 */
	public static long getFrontSquaresOnThreeFiles(final int color, final int square) {
		return FRONT_SQUARES_ON_THREE_FILES[color][square];
	}

	/**
	 * Returns mask of squares in front of given square on neighbor files.
	 *
	 * @param color  color of player
	 * @param square square
	 * @return mask of squares in front of given square
	 */
	public static long getFrontSquaresOnNeighborFiles(final int color, final int square) {
		return FRONT_SQUARES_ON_NEIGHBOR_FILES[color][square];
	}

	/**
	 * Returns mask of squares that if they would be occupied by opposite pawn
	 * stops pawn on given square.
	 *
	 * @param color  color of pawn
	 * @param square square
	 * @return mask of blocking squares
	 */
	public static long getPawnBlockingSquares(final int color, final int square) {
		return PAWN_BLOCKING_SQUARES[color][square];
	}

	public static long getSquaresInFrontInclusive(final int color, final int square) {
		return SQUARES_IN_FRONT_INCLUSIVE[color][square];
	}

	public static long getSquaresInFrontExclusive(final int color, final int square) {
		return SQUARES_IN_FRONT_EXCLUSIVE[color][square];
	}

	/**
	 * Returns one or two squares on same rank left and right to given square.
	 *
	 * @param square pawn square
	 * @return mask of neighbor squares
	 */
	public static long getConnectedPawnSquareMask(final int square) {
		return CONNECTED_PAWN_SQUARE_MASKS[square];
	}

	/**
	 * Returns union of masks obtained by calling getConnectedPawnSquareMask for
	 * every pawn in pawnMask.
	 *
	 * @param pawnMask mask of pawns
	 * @return mask of neighbor squares
	 */
	public static long getAllConnectedPawnSquareMask(final long pawnMask) {
		final long previousColumn = (pawnMask & ~BoardConstants.FILE_A_MASK) >>> 1;
		final long nextColumn = (pawnMask & ~BoardConstants.FILE_H_MASK) << 1;

		return previousColumn | nextColumn;
	}

	/**
	 * Returns mask of first rank of given side.
	 *
	 * @param color color of side
	 * @return first rank
	 */
	public static long getFirstRankMask(final int color) {
		return FIRST_RANK_MASKS[color];
	}

	/**
	 * Returns mask of first rank of given side.
	 *
	 * @param color color of side
	 * @return first rank
	 */
	public static long getSecondRankMask(final int color) {
		return SECOND_RANK_MASKS[color];
	}

	public static long getPawnsAttackedSquaresFromLeft(final int color, final long pawnsMask) {
		final long rightPawnMask = pawnsMask & RIGHT_PAWN_CAPTURE_MASK;

		if (color == Color.WHITE)
			return rightPawnMask << 9;
		else
			return rightPawnMask >>> 7;
	}

	public static long getPawnsAttackedSquaresFromRight(final int color, final long pawnsMask) {
		final long leftPawnMask = pawnsMask & LEFT_PAWN_CAPTURE_MASK;

		if (color == Color.WHITE)
			return leftPawnMask << 7;
		else
			return leftPawnMask >>> 9;
	}

	/**
	 * Returns all squares attacked by some pawn.
	 *
	 * @param color     color of the pawn
	 * @param pawnsMask pawn mask
	 * @return mask of attacked squares
	 */
	public static long getPawnsAttackedSquares(final int color, final long pawnsMask) {
		final long blackMask = (long) -color;
		final long whiteMask = ~blackMask;

		final long leftPawnMask = (pawnsMask & LEFT_PAWN_CAPTURE_MASK) >>> 1;
		final long rightPawnMask = (pawnsMask & RIGHT_PAWN_CAPTURE_MASK) << 1;
		final long combinedMask = leftPawnMask | rightPawnMask;

		return (whiteMask & (combinedMask << File.COUNT)) | (blackMask & (combinedMask >>> File.COUNT));
	}

	public static long getKingsAttackedSquares(final long kingsMask) {
		final long fileExtension = ((kingsMask & ~FILE_A_MASK) >>> 1) | ((kingsMask & ~FILE_H_MASK) << 1);
		final long fileExtended = kingsMask | fileExtension;
		final long rankExtension = ((fileExtended & ~RANK_1_MASK) >>> File.COUNT)
				| ((fileExtended & ~RANK_8_MASK) << File.COUNT);

		return rankExtension | fileExtension;
	}

	/**
	 * Returns all squares where pawns can move by single step.
	 *
	 * @param color     color of the pawn
	 * @param pawnsMask pawn mask
	 * @return mask of target squares
	 */
	public static long getPawnSingleMoveSquares(final int color, final long pawnsMask) {
		if (color == Color.WHITE)
			return pawnsMask << 8;
		else
			return pawnsMask >>> 8;
	}

	/**
	 * Returns mask of square on file preceding EP file on EP rank.
	 *
	 * @param color color of the pawn that has moved by two squares
	 * @param file  EP file
	 * @return mask of square on file preceding EP file on EP rank
	 */
	public static long getPrevEpFileMask(final int color, final int file) {
		return PREV_EP_FILE_MASK[color][file];
	}

	/**
	 * Returns mask of square on file succeeding EP file on EP rank.
	 *
	 * @param color color of the pawn that has moved by two squares
	 * @param file  EP file
	 * @return mask of square on file succeeding EP file on EP rank
	 */
	public static long getNextEpFileMask(final int color, final int file) {
		return NEXT_EP_FILE_MASK[color][file];
	}

	/**
	 * Returns mask of squares where given piece can be placed.
	 *
	 * @param pieceType type of piece
	 * @return mask of allowed squares
	 */
	public static long getPieceAllowedSquares(final int pieceType) {
		return (pieceType == PieceType.PAWN) ? PAWN_ALLOWED_SQUARES : BitBoard.FULL;
	}

	private static final byte[] MIN_FILE_DISTANCE_TABLE = initializeMinFileDistance();

	private static byte[] initializeMinFileDistance() {
		final int maxFileMask = 1 << File.LAST;
		final byte[] table = new byte[maxFileMask * File.LAST];

		for (int fileMask = 0; fileMask < maxFileMask; fileMask++) {
			for (int file = File.FIRST; file < File.LAST; file++) {
				double minDistance = File.LAST;
				int islandBeginFile = File.NONE;

				// Loop thru all bits of fileMask. We splits the files into islands and calculates the distance
				// to the middle of that islands. The trick with adding one more file ensures that
				// we will always end with bit 0 and finish the last island.
				for (int testedFile = File.FIRST; testedFile <= File.LAST; testedFile++) {
					if ((fileMask & (1 << testedFile)) != 0) {
						if (islandBeginFile == File.NONE)
							islandBeginFile = testedFile;
					}
					else {
						if (islandBeginFile != File.NONE) {
							final int islandEndFile = testedFile - 1;
							final double islandMiddleFile = (islandBeginFile + islandEndFile) / 2.0;
							minDistance = Math.min(minDistance, Math.abs(islandMiddleFile - file));
							islandBeginFile = File.NONE;
						}
					}
				}

				final int index = getMinFileDistanceIndex(fileMask, file);
				table[index] = (byte) minDistance;
			}
		}

		return table;
	}

	/**
	 * Returns distance from given file to the middle file of nearest pawn island in fileMask.
	 *
	 * @return distance
	 */
	public static int getMinFileDistance(final int fileMask, final int file) {
		final int index = getMinFileDistanceIndex(fileMask, file);

		return MIN_FILE_DISTANCE_TABLE[index];
	}

	private static int getMinFileDistanceIndex(final int fileMask, final int file) {
		return (fileMask << File.BIT_COUNT) | file;
	}

	private static final long[] KING_NEAR_SQUARES = LongArrayBuilder.create(Square.LAST)
			.fill(s -> FigureAttackTable.getItem(PieceType.KING, s) | BitBoard.getSquareMask(s))
			.build();

	/**
	 * Returns mask of squares attacked by king on given square plus given square.
	 *
	 * @param square king position
	 * @return mask of squares with king distance <= 1 from given square
	 */
	public static long getKingNearSquares(final int square) {
		return KING_NEAR_SQUARES[square];
	}

	public static long getKingSafetyFarSquares(final int square) {
		return KING_SAFETY_FAR_SQUARES[square];
	}
}
