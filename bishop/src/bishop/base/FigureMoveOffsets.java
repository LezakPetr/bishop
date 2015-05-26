package bishop.base;

public class FigureMoveOffsets {
	
    private static final FileRankOffset[] directionOffsetTable = {
    	// Rook
    	new FileRankOffset (+1, 0), new FileRankOffset (0, -1),
    	new FileRankOffset (-1, 0), new FileRankOffset (0, +1),

    	// Bishop
    	new FileRankOffset (+1, +1), new FileRankOffset (+1, -1),
    	new FileRankOffset (-1, -1), new FileRankOffset (-1, +1),
    	
    	// Knight
    	new FileRankOffset (+1, +2), new FileRankOffset (+2, +1),
    	new FileRankOffset (+2, -1), new FileRankOffset (+1, -2),
    	new FileRankOffset (-1, -2), new FileRankOffset (-2, -1),
    	new FileRankOffset (-2, +1), new FileRankOffset (-1, +2)
    };

    // These two arrays contains offsets and count of directions in directionOffsetTable
    // for each figure.
    private static final int[] figureDirectionOffsets = {0, 0, 0, 4, 8};
    private static final int[] figureDirectionCounts  = {8, 8, 4, 4, 8};
    
    // Returns number of directions where given figure can move.
    public static int getFigureDirectionCount (final int pieceType) {
    	return figureDirectionCounts[pieceType];
    }

    // Returns offset for given direction.
    public static FileRankOffset getFigureOffset (final int pieceType, final int direction) {
    	final int offset = figureDirectionOffsets[pieceType]; 
    	
    	return directionOffsetTable[offset + direction]; 
    }

}
