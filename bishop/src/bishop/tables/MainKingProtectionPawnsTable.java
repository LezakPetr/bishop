package bishop.tables;

import bishop.base.Square;

public class MainKingProtectionPawnsTable extends KingProtectionPawnsTable {
	
	private static final long[] table = createTable();
	
	
	public static long getItem (final int color, final int castlingType) {
		final int index = getItemIndex(color, castlingType);
		
		return table[index];
	}

	private static long[] createTable() {
    	final int[][][] SQUARES = {
    		{
    			{ Square.F2, Square.G2, Square.H2 },
    			{ Square.A2, Square.B2, Square.C2 }
    		},
    		{
    			{ Square.F7, Square.G7, Square.H7 },
    			{ Square.A7, Square.B7, Square.C7 }
    		}
    	};

    	return createTableWithSquares(SQUARES);
	}
	
	
}
