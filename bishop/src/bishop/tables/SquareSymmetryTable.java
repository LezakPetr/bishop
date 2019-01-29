package bishop.tables;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.File;
import bishop.base.Rank;
import bishop.base.Square;
import bishop.base.Symmetry;

public class SquareSymmetryTable {
	
	private static final int SQUARE_SHIFT = 0;
	private static final int SYMMETRY_SHIFT = SQUARE_SHIFT + Square.BIT_COUNT;
	private static final int BIT_COUNT = SYMMETRY_SHIFT + Symmetry.BIT_COUNT;
	
	
	private static final int[] table = createTable();
	
	private static int getItemIndex (final int symmetry, final int square) {
		return square + (symmetry << SYMMETRY_SHIFT);
	}
	
	public static int getItem (final int symmetry, final int square) {
		final int index = getItemIndex(symmetry, square);
		
		return table[index];
	}

	private static int[] createTable() {
		final int size = 1 << BIT_COUNT;
		final int[] table = new int[size];
		
    	for (int symmetry = Symmetry.FIRST; symmetry < Symmetry.LAST; symmetry++) {
    		for (int square = Square.FIRST; square < Square.LAST; square++) {
    			int file = Square.getFile(square);
    			int rank = Square.getRank(square);
    			
    			if ((symmetry & Symmetry.ROTATE_MASK) != 0) {
    				final int pomRank = rank;
    				
    				rank = file;
    				file = Rank.R8 - pomRank;
    			}
    			
    			if ((symmetry & Symmetry.FLIP_FILE_MASK) != 0) {
    				file = File.FH - file;
    			}
    			
    			if ((symmetry & Symmetry.FLIP_RANK_MASK) != 0) {
    				rank = Rank.R8 - rank;
    			}
    			
    			table[getItemIndex(symmetry, square)] = Square.onFileRank(file, rank);
    		}
    	}
    	
    	return table;
	}

	public static long transformBoard(final int symmetry, final long origBoard) {
		long result = BitBoard.EMPTY;
		
		for (BitLoop loop = new BitLoop(origBoard); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			final int transformedSquare = getItem(symmetry, square);
			
			result |= BitBoard.getSquareMask(transformedSquare);
		}
				
		return result;
	}
}
