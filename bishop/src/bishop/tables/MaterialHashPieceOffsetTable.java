package bishop.tables;

import bishop.base.Color;
import bishop.base.MaterialHash;
import bishop.base.PieceType;

public class MaterialHashPieceOffsetTable {
	
	private static final int COLOR_SHIFT = 0;
	private static final int PIECE_TYPE_SHIFT = COLOR_SHIFT + Color.BIT_COUNT;
	private static final int SIZE = PieceType.LAST * (1 << PIECE_TYPE_SHIFT);
	
	private static final int[] table = createTable();
	
	
	private static int getItemIndex (final int color, final int pieceType) {
		return color + (pieceType << PIECE_TYPE_SHIFT);
	}
	
	public static int getItem (final int color, final int pieceType) {
		final int index = getItemIndex (color, pieceType);
		
		return table[index];
	}
	
	public static int getMaterialHashPieceOffset(final int color, final int pieceType) {
		if (PieceType.isVariablePiece (pieceType)) {
			int index = ((pieceType - PieceType.VARIABLE_FIRST) << Color.BIT_COUNT) | color;
			index = index * MaterialHash.BITS_PER_ITEM;
			
			return index;
		}
		else
			return -1;		
	}

	private static int[] createTable() {
    	final int[] table = new int[SIZE];
    	
    	for (int color = Color.FIRST; color < Color.LAST; color++) {
    		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
    			final int coeff = getMaterialHashPieceOffset(color, pieceType);
    			
    			table[getItemIndex(color, pieceType)] = coeff;
    		}
    	}
    	
    	return table;
	}

}
