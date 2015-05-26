package bishop.base;

public class PieceMoveTables {

	private static final EpMoveRecord[][][] epMoveRecords = initializeEpMoveRecords();
	

	/**
	 * Initializes table of EP move records.
	 * @return created table
	 */
	private static EpMoveRecord[][][] initializeEpMoveRecords() {
		final int[] beginRanks = new int[Color.LAST];
		
		beginRanks[Color.WHITE] = Rank.R5;
		beginRanks[Color.BLACK] = Rank.R4;
		
		final int[] targetRanks = new int[Color.LAST];

		targetRanks[Color.WHITE] = Rank.R6;
		targetRanks[Color.BLACK] = Rank.R3;
		
		final EpMoveRecord[][][] table = new EpMoveRecord[Color.LAST][File.LAST][EpDirection.LAST];

		for (int color = Color.FIRST; color < Color.LAST; color++) {
		    for (int file = File.FIRST; file < File.LAST; file++) {
		    	// Left en-passant
		    	final EpMoveRecord leftRrecord;
		    	
		    	if (file > File.FA)
		    		leftRrecord = new EpMoveRecord(Square.onFileRank(file-1, beginRanks[color]), Square.onFileRank(file, targetRanks[color]));
		    	else
		    		leftRrecord = null;
		    	
		    	table[color][file][EpDirection.LEFT] = leftRrecord;

		    	// Right en-passant
		    	final EpMoveRecord rightRrecord;
		    	
		    	if (file < File.FH)
		    		rightRrecord = new EpMoveRecord(Square.onFileRank(file+1, beginRanks[color]), Square.onFileRank(file, targetRanks[color]));
		    	else
		    		rightRrecord = null;
		    	
		    	table[color][file][EpDirection.RIGHT] = rightRrecord;
		    }
		}
		
		return table;
	}
	

	/**
	 * Returns EP record for given color, EP file and direction.
	 * @param color color of pawn
	 * @param epFile file where pawn has moved by two squares
	 * @param direction EP direction
	 * @return required move record
	 */
	public static EpMoveRecord getEpMoveRecord (final int color, final int epFile, final int direction) {
		return epMoveRecords[color][epFile][direction];
	}

}
