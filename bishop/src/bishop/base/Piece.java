package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;

import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import utils.IoUtils;

public enum Piece {

	WHITE_PAWN(Color.WHITE, PieceType.PAWN),
	BLACK_PAWN(Color.BLACK, PieceType.PAWN),
	WHITE_KNIGHT(Color.WHITE, PieceType.KNIGHT),
	BLACK_KNIGHT(Color.BLACK, PieceType.KNIGHT),
	WHITE_BISHOP(Color.WHITE, PieceType.BISHOP),
	BLACK_BISHOP(Color.BLACK, PieceType.BISHOP),
	WHITE_ROOK(Color.WHITE, PieceType.ROOK),
	BLACK_ROOK(Color.BLACK, PieceType.ROOK),
	WHITE_QUEEN(Color.WHITE, PieceType.QUEEN),
	BLACK_QUEEN(Color.BLACK, PieceType.QUEEN),
	WHITE_KING(Color.WHITE, PieceType.KING),
	BLACK_KING(Color.BLACK, PieceType.KING);

	public static final Piece EMPTY = null;
	
	private final int color;
	private final int pieceType;

	/**
	 * Creates piece with given color and type.
	 * @param color color of piece
	 * @param pieceType type of piece
	 */
	private Piece (final int color, final int pieceType) {
		this.color = color;
		this.pieceType = pieceType;
	}
	
	/**
	 * Returns color of piece.
	 * @return color of piece
	 */
	public int getColor() {
		return color;
	}
	
	/**
	 * Returns type of piece.
	 * @return type of piece
	 */
	public int getPieceType() {
		return pieceType;
	}

	// Table of precreated instances
	private static final Piece[][] instances = initializeInstances();
	
	/**
	 * Creates instances of Piece.
	 * @return precreated instances
	 */
	private static final Piece[][] initializeInstances() {
		final Piece[][] table = new Piece[Color.LAST][PieceType.LAST];
		
		for (Piece piece: values()) {
			final int color = piece.getColor();
			final int pieceType = piece.getPieceType();
			table[color][pieceType] = piece;
		}
		
		return table;
	}

	/**
	 * Returns piece with given color and type.
	 * @param color color of piece
	 * @param pieceType type of piece
	 * @return precreated instance of piece
	 */
	public static Piece withColorAndType (final int color, final int pieceType) {
		return instances[color][pieceType];
	}
	
	/**
	 * Writes given piece into given writer.
	 * @param writer PrintWriter
	 */
	public void write (final PrintWriter writer) {
		final boolean upperCase = (color == Color.WHITE);
		
		PieceType.write(writer, pieceType, upperCase);
	}
	
	/**
	 * Reads piece from stream.
	 * @param reader PushbackReader
	 * @return piece
	 * @throws IOException when IO failure occurs or when read character does not represent some piece
	 */
	public static Piece read (final PushbackReader reader) throws IOException {
		final char c = IoUtils.readChar(reader);
		final boolean upperCase = Character.isUpperCase(c);
		
		reader.unread(c);
		
		final int pieceType = PieceType.read(reader);
		final int color = (upperCase) ? Color.WHITE : Color.BLACK;
		
		return Piece.withColorAndType(color, pieceType);
	}
	
	@Override
	public String toString() {
		return Color.getName(color) + " " + PieceType.getName(pieceType);
	}
	
	public long getAttackedSquares (final int square) {
		if (pieceType == PieceType.PAWN)
			return PawnAttackTable.getItem(color, square);
		else
			return FigureAttackTable.getItem(pieceType, square);
	}
	
	public static final int LAST_PROMOTION_FIGURE_INDEX = (PieceType.PROMOTION_FIGURE_LAST - PieceType.PROMOTION_FIGURE_FIRST) << Color.BIT_COUNT;
	
	public static int getPromotionFigureIndex (final int color, final int pieceType) {
		return ((pieceType - PieceType.PROMOTION_FIGURE_FIRST) << Color.BIT_COUNT) + color;
	}
	

}
