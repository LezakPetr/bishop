package bishop.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.IoUtils;

public final class Piece {

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
	private static final List<Piece> instanceList = initializeInstanceList();
	
	/**
	 * Creates instances of Piece.
	 * @return precreated instances
	 */
	private static final Piece[][] initializeInstances() {
		final Piece[][] table = new Piece[Color.LAST][PieceType.LAST];
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++)
				table[color][pieceType] = new Piece (color, pieceType);
		}
		
		return table;
	}
	
	private static List<Piece> initializeInstanceList() {
		final List<Piece> list = new ArrayList<Piece>();
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++)
				list.add(Piece.withColorAndType(color, pieceType));
		}
		
		return Collections.unmodifiableList(list);
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
	
	public static List<Piece> getAllPieces() {
		return instanceList; 
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
	
	public static final int LAST_PROMOTION_FIGURE_INDEX = (PieceType.PROMOTION_FIGURE_LAST - PieceType.PROMOTION_FIGURE_FIRST) << Color.BIT_COUNT;
	
	public static int getPromotionFigureIndex (final int color, final int pieceType) {
		return ((pieceType - PieceType.PROMOTION_FIGURE_FIRST) << Color.BIT_COUNT) + color;
	}
	

}
