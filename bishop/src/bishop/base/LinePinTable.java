package bishop.base;

import bishop.tables.BetweenTable;

/**
 * This table contains squares attacked from some square with some occupancy.
 * @author Ing. Petr Ležák
 */
public class LinePinTable {
	private static long[] table = initializeTable();

	private static long[] initializeTable() {
		final long[] table = new long[LineIndexer.getLastIndex()];
		
		for (int direction = CrossDirection.FIRST; direction < CrossDirection.LAST; direction++) {
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				long mask = BitBoard.EMPTY;
				
				for (FileRankOffset offset: LineIndexer.getDirectionOffsets(direction)) {
					mask |= getLineMask(square, offset);
				}
				
				for (BitBoardCombinator combinator = new BitBoardCombinator(mask); combinator.hasNextCombination(); ) {
					final long occupancy = combinator.getNextCombination();
					final long attackMask = getAttackMask(square, mask, occupancy);
					
					final int index = LineIndexer.getLineIndex(direction, square, occupancy);
					table[index] = attackMask;
				}
			}
		}
		
		return table;
	}
	
	/**
	 * Returns mask of attacked squares.
	 * @param index line index
	 * @return mask of attacked squares
	 */
	public static long getItem (final int index) {
		return table[index];
	}
	
	public static long getLineMask (final int square, final FileRankOffset offset) {
	    long mask = 0;

    	int targetFile = Square.getFile(square) + offset.getFileOffset();
    	int targetRank = Square.getRank(square) + offset.getRankOffset();

    	while (File.isValid(targetFile) && Rank.isValid(targetRank)) {
    		final int targetSquare = Square.onFileRank(targetFile, targetRank);
    		mask |= BitBoard.getSquareMask(targetSquare);

    		targetFile += offset.getFileOffset();
    		targetRank += offset.getRankOffset();
    	}
    	
    	return mask;
	}
	
	private static long getAttackMask (final int square, final long cross, final long occupancy) {
		long attackMask = BitBoard.EMPTY;
		
		for (BitLoop loop = new BitLoop(cross); loop.hasNextSquare(); ) {
			final int targetSquare = loop.getNextSquare();
			
			if ((BetweenTable.getItem(square, targetSquare) & occupancy) == 0)
				attackMask |= BitBoard.getSquareMask(targetSquare);
		}
		
		return attackMask;
	}
}
