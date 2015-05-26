package bishop.tables;

import bishop.base.Color;
import bishop.base.HashConstants;
import bishop.base.PieceType;
import bishop.base.Square;

public class PieceHashTable {
	
	private static final int SQUARE_SHIFT = 0;
	private static final int COLOR_SHIFT = SQUARE_SHIFT + Square.BIT_COUNT;
	private static final int PIECE_TYPE_SHIFT = COLOR_SHIFT + Color.BIT_COUNT;
	private static final int SIZE = PieceType.LAST * (1 << PIECE_TYPE_SHIFT);
	
	private static int getItemIndex (final int color, final int pieceType, final int square) {
		return square + (color << COLOR_SHIFT) + (pieceType << PIECE_TYPE_SHIFT); 
	}
	
	public static long getItem (final int color, final int pieceType, final int square) {
		final int index = getItemIndex(color, pieceType, square);
		
		return table[index];
	}
	
	private static final long[] table = createTable();
	
	private static long[] createTable() {
		final long[] table = new long[SIZE];
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				for (int square = Square.FIRST; square < Square.LAST; square++) {
					final long hash = HashConstants.calculateHashConstant(HashConstants.TYPE_PIECES, color, pieceType, square);
					
					table[getItemIndex (color, pieceType, square)] = hash;
				}
			}
		}
		
		return table;
	}
	
	
}
