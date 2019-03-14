package bishop.tablebase;

import bishop.base.Color;
import bishop.base.Piece;

/**
 * Immutable definition of combination of same pieces.
 * @author Ing. Petr Ležák
 */
public class CombinationDefinition {

	public static final CombinationDefinition[] EMPTY_ARRAY = new CombinationDefinition[0];
	
	private static final int COLOR_MASK = 0x01;
	private static final int PIECE_TYPE_MASK = 0x0E;
	private static final int COUNT_MASK = 0xF0;
	
	private static final int COLOR_SHIFT = 0;
	private static final int PIECE_TYPE_SHIFT = 1;
	private static final int COUNT_SHIFT = 4;
	
	
	private final Piece piece;
	private final int count;
	
	public CombinationDefinition(final Piece piece, final int count) {
		this.piece = piece;
		this.count = count;
	}
	
	public Piece getPiece() {
		return piece;
	}
	
	public int getCount() {
		return count;
	}
	
	public byte toData() {
		byte data = 0;
		
		data |= piece.getColor() << COLOR_SHIFT;
		data |= piece.getPieceType() << PIECE_TYPE_SHIFT;
		data |= count << COUNT_SHIFT;
		
		return data;
	}
	
	public static CombinationDefinition fromData(final byte data) {
		final int color = (data & COLOR_MASK) >>> COLOR_SHIFT;
		final int pieceType = (data & PIECE_TYPE_MASK) >>> PIECE_TYPE_SHIFT;
		final int count = (data & COUNT_MASK) >>> COUNT_SHIFT;
		
		final Piece piece = Piece.withColorAndType(color, pieceType);
		
		return new CombinationDefinition(piece, count);
	}

	public CombinationDefinition getAfterCapture() {
		return getWithoutSomePieces(1);
	}
	
	public CombinationDefinition getWithoutSomePieces (final int removedPieceCount) {
		return new CombinationDefinition(piece, count - removedPieceCount);
	}

	public CombinationDefinition getOpposite() {
		final int oppositeColor = Color.getOppositeColor(piece.getColor());
		final Piece oppositePiece = Piece.withColorAndType(oppositeColor, piece.getPieceType());
		
		return new CombinationDefinition(oppositePiece, count);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof CombinationDefinition))
			return false;
		
		final CombinationDefinition cmp = (CombinationDefinition) obj;
		
		return this.piece == cmp.piece && this.count == cmp.count;
	}
	
	@Override
	public int hashCode() {
		return toData();
	}

	@Override
	public String toString() {
		return "Piece: " + piece.toString() + ", Count: " + count; 
	}

}
