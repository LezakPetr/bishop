package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import utils.IoUtils;

public class PieceType {
	
	public static final int FIRST = 0;
	
	public static final int KING = 0;
	public static final int QUEEN = 1;
	public static final int ROOK = 2;
	public static final int BISHOP = 3;
	public static final int KNIGHT = 4;
	public static final int PAWN = 5;
	
	public static final int LAST = 6;
	public static final int NONE = 6;   // I must use value that fits into 3 bits because Move needs it
	
	// Subinterval of figures (pieces without pawn)
	public static final int FIGURE_FIRST = 0;
	public static final int FIGURE_LAST = 5;
	public static final int FIGURE_COUNT = FIGURE_LAST - FIGURE_FIRST;
	
	// Subinterval of figures that can pawn promote to
	public static final int PROMOTION_FIGURE_FIRST = 1;
	public static final int PROMOTION_FIGURE_LAST  = 5;
	public static final int PROMOTION_FIGURE_COUNT = PROMOTION_FIGURE_LAST - PROMOTION_FIGURE_FIRST;
	
	// Subinterval of variable pieces (all pieces without king which is permanent).
	public static final int VARIABLE_FIRST = 1;
	public static final int VARIABLE_LAST = 6;
	public static final int VARIABLE_COUNT = PieceType.VARIABLE_LAST - PieceType.VARIABLE_FIRST;
	
	
	private static final char[] NOTATION = {'k', 'q', 'r', 'b', 'n', 'p'};
	
	public static final String NAME_KING = "king";
	public static final String NAME_QUEEN = "queen";
	public static final String NAME_ROOK = "rook";
	public static final String NAME_BISHOP = "bishop";
	public static final String NAME_KNIGHT = "knight";
	public static final String NAME_PAWN = "pawn";
	
	private static final String[] PIECE_TYPE_NAMES = {
		NAME_KING, NAME_QUEEN, NAME_ROOK, NAME_BISHOP, NAME_KNIGHT, NAME_PAWN
	};

	
	/**
	 * Checks if given piece type is valid.
	 * @param pieceType piece type
	 * @return true if piece type is valid, false if not
	 */
	public boolean isValid (final int pieceType) {
		return pieceType >= FIRST && pieceType < LAST;
	}	
	
	/**
	 * Writes given piece type into given writer.
	 * @param writer PrintWriter
	 * @param pieceType type of piece
	 * @param upperCase if piece type should be written upper case
	 */
	public static void write (final PrintWriter writer, final int pieceType, final boolean upperCase) {
		final char c = toChar(pieceType, upperCase);
		
		writer.print(c);
	}

	public static char toChar(final int pieceType, final boolean upperCase) {
		char c = NOTATION[pieceType];
		
		if (upperCase)
			c = Character.toUpperCase(c);
		
		return c;
	}
	
	public static int read (final Reader reader) throws IOException {
		final char ch = IoUtils.readChar(reader);
		
		return fromChar(ch);
	}

	public static int fromChar(final char pieceChar) {
		final char lowerCasePieceChar = Character.toLowerCase(pieceChar);
		
		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
			if (lowerCasePieceChar == NOTATION[pieceType])
				return pieceType;
		}
		
		throw new RuntimeException("Unknown piece type");
	}
	
	/**
	 * Checks if given pieceType is figure.
	 * @param pieceType type of piece
	 * @return true if given piece type is figure, false if not or if piece type is not valid
	 */
	public static boolean isFigure (final int pieceType) {
		return pieceType >= FIGURE_FIRST && pieceType < FIGURE_LAST;
	}	

	/**
	 * Checks if given pieceType is short moving figure.
	 * @param pieceType type of piece
	 * @return true if given piece type is short moving figure, false if not or if piece type is not valid
	 */
	public static boolean isShortMovingFigure (final int pieceType) {
	    return pieceType == KING || pieceType == KNIGHT;
	}

	public static boolean isVariablePiece(final int pieceType) {
		return pieceType >= VARIABLE_FIRST && pieceType < VARIABLE_LAST;
	}

	public static boolean isPromotionFigure(final int pieceType) {
		return pieceType >= PROMOTION_FIGURE_FIRST && pieceType < PROMOTION_FIGURE_LAST;
	}

	public static String getName(final int pieceType) {
		return PIECE_TYPE_NAMES[pieceType];
	}
}
