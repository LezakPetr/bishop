package bishop.tables;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.PieceType;

public class MaterialHashPieceMaskTable {
	
	private static final int COLOR_SHIFT = 0;
	private static final int PIECE_TYPE_SHIFT = COLOR_SHIFT + Color.BIT_COUNT;
	private static final int SIZE = PieceType.LAST * (1 << PIECE_TYPE_SHIFT);
	
	private static final long[] table = createTable();
	
	
	private static int getItemIndex (final int color, final int pieceType) {
		return color + (pieceType << PIECE_TYPE_SHIFT);
	}
	
	public static long getItem (final int color, final int pieceType) {
		final int index = getItemIndex (color, pieceType);
		
		return table[index];
	}
	
	public static long getMaterialHashPieceMask(final int color, final int pieceType) {
		if (PieceType.isVariablePiece (pieceType)) {
			final long shift = MaterialHashPieceOffsetTable.getMaterialHashPieceOffset(color, pieceType);
			long mask = 0;
			
			for (int i = 0; i < MaterialHash.BITS_PER_ITEM; i++) {
				mask |= 1L << (shift + i);
			}
			
			return mask;
		}
		else
			return 0;
	}
	
	private static long[] createTable() {
    	final long[] table = new long[SIZE];
    	
    	for (int color = Color.FIRST; color < Color.LAST; color++) {
    		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
    			final long coeff = getMaterialHashPieceMask(color, pieceType);
    			
    			table[getItemIndex (color, pieceType)] = coeff;
    		}
    	}
    	
    	return table;
	}

}
