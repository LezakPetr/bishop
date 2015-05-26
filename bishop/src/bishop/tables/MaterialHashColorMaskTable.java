package bishop.tables;

import bishop.base.Color;
import bishop.base.PieceType;

public class MaterialHashColorMaskTable {
	
	private static final long[] table = createTable();
	

	public static long getItem (final int color) {
		return table[color];
	}

	private static long[] createTable() {
		final long[] table = new long[Color.LAST];
		
    	for (int color = Color.FIRST; color < Color.LAST; color++) {
    		long mask = 0;
    		
    		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
    			mask |= MaterialHashPieceMaskTable.getMaterialHashPieceMask(color, pieceType);
    		}
    		
    		table[color] = mask;
    	}

		return table;
	}
	
}
