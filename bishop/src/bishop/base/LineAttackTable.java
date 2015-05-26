package bishop.base;

import bishop.tables.BetweenTable;

/**
 * This table contains squares attacked from some square with some occupancy.
 * @author Ing. Petr Ležák
 */
public class LineAttackTable {
	
	private static long[] attackTable = initializeTable(0);
	private static long[] pinTable = initializeTable(1);
	
	/**
	 * Returns mask of attacked squares.
	 * @param index line index
	 * @return mask of attacked squares
	 */
	public static long getAttackMask (final int index) {
		return attackTable[index];
	}

	/**
	 * Returns mask of squares blocked by one piece.
	 * @param index line index
	 * @return mask of attacked squares with one blocking piece 
	 */
	public static long getPinMask (final int index) {
		return pinTable[index];
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
		
	private static long[] initializeTable(final int betweenCount) {
		final long[] table = new long[LineIndexer.getLastIndex()];
		
		for (int direction = CrossDirection.FIRST; direction < CrossDirection.LAST; direction++) {
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				long mask = BitBoard.EMPTY;
				
				for (FileRankOffset offset: LineIndexer.getDirectionOffsets(direction)) {
					mask |= LineAttackTable.getLineMask(square, offset);
				}
				
				for (BitBoardCombinator combinator = new BitBoardCombinator(mask); combinator.hasNextCombination(); ) {
					final long occupancy = combinator.getNextCombination();
					final long attackMask = getAttackMask(square, mask, occupancy, betweenCount);
					
					final int index = LineIndexer.getLineIndex(direction, square, occupancy);
					table[index] = attackMask;
				}
			}
		}
		
		return table;
	}

	private static long getAttackMask (final int square, final long cross, final long occupancy, final int betweenCount) {
		long attackMask = BitBoard.EMPTY;
		
		for (BitLoop loop = new BitLoop(cross); loop.hasNextSquare(); ) {
			final int targetSquare = loop.getNextSquare();
			final long betweenOccupiedSquares = BetweenTable.getItem(square, targetSquare) & occupancy;
			
			if (BitBoard.getSquareCount(betweenOccupiedSquares) == betweenCount)
				attackMask |= BitBoard.getSquareMask(targetSquare);
		}
		
		return attackMask;
	}

}
