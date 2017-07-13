package bishop.tables;

import bishop.base.Square;

public class SecondKingProtectionPawnsTable extends KingProtectionPawnsTable {

	
	
	private static final long[] table = createTable();


	public static long getItem (final int color, final int castlingType) {
		final int index = getItemIndex (color, castlingType);
		
		return table[index];
	}
	
	
	private static long[] createTable() {
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
    	    	
    	return createTableWithSquares(SQUARES);
	}
}
