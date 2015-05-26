package bishop.tables;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.Rank;
import bishop.base.Square;

public class FrontSquaresOnSameFileTable {
	
	private static final int SQUARE_SHIFT = 0;
	private static final int COLOR_SHIFT = SQUARE_SHIFT + Square.BIT_COUNT;
	private static final int BIT_COUNT = COLOR_SHIFT + Color.BIT_COUNT;
	
	
	private static int getItemIndex (final int color, final int square) {
		return square + (color << COLOR_SHIFT);
	}
	
	public static long getItem (final int color, final int square) {
		final int index = getItemIndex(color, square);
		
		return table[index];
	}
	
	private static final long[] table = createTable();

	private static long[] createTable() {
		final int size = 1 << BIT_COUNT;
		final long[] table = new long[size];
		
    	for (int pawnColor = Color.FIRST; pawnColor < Color.LAST; pawnColor++) {
    		final int rankOffset = BoardConstants.getPawnRankOffset(pawnColor);
    		
    		for (int pawnSquare = Square.FIRST; pawnSquare < Square.LAST; pawnSquare++) {
    			final int file = Square.getFile(pawnSquare);
    			
    			long board = BitBoard.EMPTY;
    			int rank = Square.getRank(pawnSquare);
    			rank += rankOffset;
    			
    			while (Rank.isValid(rank)) {
    				final int square = Square.onFileRank(file, rank);
    				board |= BitBoard.getSquareMask(square);
    				rank += rankOffset;
    			}
    			
    			table[getItemIndex(pawnColor, pawnSquare)] = board;
    		}
    	}
		
		return table;
	}
}
