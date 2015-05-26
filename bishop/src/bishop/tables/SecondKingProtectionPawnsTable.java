package bishop.tables;

import bishop.base.BitBoard;
import bishop.base.CastlingType;
import bishop.base.Color;
import bishop.base.Square;

public class SecondKingProtectionPawnsTable {

	private static final int CASTLING_TYPE_SHIFT = 0;
	private static final int COLOR_SHIFT = CASTLING_TYPE_SHIFT + CastlingType.BIT_COUNT;
	private static final int BIT_COUNT = COLOR_SHIFT + Color.BIT_COUNT;
	
	
	private static final long[] table = createTable();

	
	private static final int getItemIndex (final int color, final int castlingType) {
		return castlingType + (color << COLOR_SHIFT);
	}

	public static long getItem (final int color, final int castlingType) {
		final int index = getItemIndex (color, castlingType);
		
		return table[index];
	}
	
	
	private static long[] createTable() {
		final int size = 1 << BIT_COUNT;
		final long[] table = new long[size];
		
    	final int[][][] SQUARES = {
    		{
    			{ Square.G3, Square.H3 },
    			{ Square.A3, Square.B3 }
    		},
    		{
    			{ Square.G6, Square.H6 },
    			{ Square.A6, Square.B6 }
    		}
    	};
    	
    	for (int color = Color.FIRST; color < Color.LAST; color++) {
    		for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
    			long mask = 0;
    			
    			for (int square: SQUARES[color][castlingType])
    				mask |= BitBoard.getSquareMask(square);
    			
    	    	table[getItemIndex (color, castlingType)] = mask;
    		}
    	}
    	
    	return table;
	}
}
