package bishop.tables;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.Rank;
import bishop.base.Square;

public class PawnMoveTable {
	
	private static final int SQUARE_SHIFT = 0;
	private static final int COLOR_SHIFT = SQUARE_SHIFT + Square.BIT_COUNT;
	private static final int BIT_COUNT = COLOR_SHIFT + Square.BIT_COUNT;
	
	
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
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int square = Square.FIRST; square < Square.LAST; square++) {
		    	final int file = Square.getFile(square);
		    	final int rank = Square.getRank(square);

		    	long board = 0;

		    	if (rank >= Rank.R2 && rank <= Rank.R7) {
			        final int rankOffset = BoardConstants.getPawnRankOffset(color);

			        board |= BitBoard.getSquareMask(Square.onFileRank(file, rank + rankOffset));

			        if ((color == Color.WHITE && rank == Rank.R2) || (color == Color.BLACK && rank == Rank.R7))
			        	board |= BitBoard.getSquareMask(Square.onFileRank(file, rank + 2*rankOffset));
		    	}

		    	table[getItemIndex (color, square)] = board;
			}
		}
		
		return table;
	}
	
}
