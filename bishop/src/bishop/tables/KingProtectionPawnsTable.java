package bishop.tables;


import bishop.base.BitBoard;
import bishop.base.CastlingType;
import bishop.base.Color;

public class KingProtectionPawnsTable {
	
	private static final int CASTLING_TYPE_SHIFT = 0;
	private static final int COLOR_SHIFT = CASTLING_TYPE_SHIFT + CastlingType.BIT_COUNT;
	private static final int BIT_COUNT = COLOR_SHIFT + Color.BIT_COUNT;
	
	
	protected static int getItemIndex (final int color, final int castlingType) {
		return castlingType + (color << COLOR_SHIFT);
	}


	protected static long[] createTableWithSquares(final int[][][] squares) {
		final int size = 1 << BIT_COUNT;
		final long[] table = new long[size];
	
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
				long mask = 0;
				
				for (int square: squares[color][castlingType])
					mask |= BitBoard.getSquareMask(square);
				
				table[getItemIndex(color, castlingType)] = mask;
			}
		}
		
		return table;
	}

}
