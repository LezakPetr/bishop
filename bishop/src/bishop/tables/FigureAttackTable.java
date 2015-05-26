package bishop.tables;

import bishop.base.BitBoard;
import bishop.base.FigureMoveOffsets;
import bishop.base.File;
import bishop.base.FileRankOffset;
import bishop.base.LineAttackTable;
import bishop.base.PieceType;
import bishop.base.Rank;
import bishop.base.Square;

public class FigureAttackTable {
	
	private static final int SQUARE_SHIFT = 0;
	private static final int PIECE_TYPE_SHIFT = SQUARE_SHIFT + Square.BIT_COUNT;
	private static final int SIZE = PieceType.FIGURE_LAST * (1 << PIECE_TYPE_SHIFT);
	
	
	private static int getItemIndex (final int pieceType, final int square) {
		return square + (pieceType << PIECE_TYPE_SHIFT);
	}
	
	public static long getItem (final int pieceType, final int square) {
		final int index = getItemIndex(pieceType, square);
		
		return table[index];
	}
	
	private static final long[] table = createTable();

	private static long[] createTable() {
		final long[] table = new long[SIZE];
		
		for (int figure = PieceType.FIGURE_FIRST; figure < PieceType.FIGURE_LAST; figure++) {
			if (PieceType.isShortMovingFigure(figure))
				initializeShortMoveFigureAttacks(figure, table);
			else
				initializeLongMoveFigureAttacks(figure, table);
		}
		
		return table;
    }
    
	/**
	 * Initializes table of short move figure attacks.
	 * @param pieceType type of piece
	 * @param table created table to fill
	 */
	private static void initializeShortMoveFigureAttacks(final int pieceType, final long[] table) {
		final int directionCount = FigureMoveOffsets.getFigureDirectionCount(pieceType);

		for (int beginSquare = Square.FIRST; beginSquare < Square.LAST; beginSquare++) {
			final int beginFile = Square.getFile(beginSquare);
			final int beginRank = Square.getRank(beginSquare);
		    
		    long board = 0;

		    for (int direction = 0; direction < directionCount; direction++) {
		    	final FileRankOffset offset = FigureMoveOffsets.getFigureOffset (pieceType, direction);

		    	final int targetFile = beginFile + offset.getFileOffset();
		    	final int targetRank = beginRank + offset.getRankOffset();
		    	
		    	if (File.isValid(targetFile) && Rank.isValid(targetRank)) {
		    		final int targetSquare = Square.onFileRank(targetFile, targetRank);
		    		board |= BitBoard.getSquareMask(targetSquare);
		    	}
		    }

		    table[getItemIndex(pieceType, beginSquare)] = board;
		}
	}
	
	/**
	 * Initializes table of long move figure attacks.
	 * @param pieceType type of piece
	 * @param table created table to fill
	 */
	private static void initializeLongMoveFigureAttacks(final int pieceType, final long[] table) {
		final int directionCount = FigureMoveOffsets.getFigureDirectionCount(pieceType);

		for (int beginSquare = Square.FIRST; beginSquare < Square.LAST; beginSquare++) {
		    long board = 0;

		    for (int direction = 0; direction < directionCount; direction++) {
		    	final FileRankOffset offset = FigureMoveOffsets.getFigureOffset (pieceType, direction);
		    	
		    	board |= LineAttackTable.getLineMask(beginSquare, offset);
		    }

		    table[getItemIndex(pieceType, beginSquare)] = board;
		}
	}
	
}
