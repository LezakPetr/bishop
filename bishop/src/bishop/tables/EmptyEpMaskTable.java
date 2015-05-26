package bishop.tables;

import bishop.base.BitBoard;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.Rank;
import bishop.base.Square;

public class EmptyEpMaskTable {
	
	private static final int FILE_SHIFT = 0;
	private static final int COLOR_SHIFT = FILE_SHIFT + File.BIT_COUNT;
	private static final int BIT_COUNT = COLOR_SHIFT + Color.BIT_COUNT;
	
	private static final int getItemIndex (final int color, final int file) {
		return file + (color << COLOR_SHIFT);
	}
	
	public static long getItem (final int color, final int file) {
		final int index = getItemIndex(color, file);
		
		return table[index];
	}
	
	private static final long[] table = createTable();

	private static long[] createTable() {
    	final int[][] EMPTY_RANKS = {
    		{Rank.R2, Rank.R3},
    		{Rank.R7, Rank.R6}
    	};

    	final int size = 1 << BIT_COUNT;
		final long[] table = new long[size];
    	
    	for (int color = Color.FIRST; color < Color.LAST; color++) {
    		for (int file = File.FIRST; file < File.LAST; file++) {
    			long mask = 0;
    			
    			for (int rank: EMPTY_RANKS[color]) {
    				final int square = Square.onFileRank(file, rank);
    				mask |= BitBoard.getSquareMask(square);
    			}
    			
    			table[getItemIndex(color, file)] = mask;
    		}
    	}
    	
    	return table;
	}
	
}
