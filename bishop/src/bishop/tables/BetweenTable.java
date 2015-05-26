package bishop.tables;

import bishop.base.BitBoard;
import bishop.base.FigureMoveOffsets;
import bishop.base.File;
import bishop.base.FileRankOffset;
import bishop.base.PieceType;
import bishop.base.Rank;
import bishop.base.Square;

/**
 * For two squares on same file, rank or diagonal this table contains mask of squares
 * between given squares (exclusive). For any other squares it contains zeros.
 * @author Ing. Petr Ležák
 */
public class BetweenTable {
	
	private static final int SQUARE1_SHIFT = 0;
	private static final int SQUARE2_SHIFT = SQUARE1_SHIFT + Square.BIT_COUNT;
	private static final int BIT_COUNT = SQUARE2_SHIFT + Square.BIT_COUNT;
	
	private static final long[] table = createTable();
	
	private static final int getItemIndex (final int square1, final int square2) {
		return square1 + (square2 << SQUARE2_SHIFT);
	}
	
	private static long[] createTable() {
		final int size = 1 << BIT_COUNT;
		final long[] table = new long[size];
		
		final int figure = PieceType.QUEEN;
		final int directionCount = FigureMoveOffsets.getFigureDirectionCount (figure);

		for (int beginSquare = Square.FIRST; beginSquare < Square.LAST; beginSquare++) {
			for (int direction = 0; direction < directionCount; direction++) {
				final FileRankOffset offset = FigureMoveOffsets.getFigureOffset (figure, direction);
				long mask = 0;

				int file = Square.getFile(beginSquare) + offset.getFileOffset();
				int rank = Square.getRank(beginSquare) + offset.getRankOffset();

				while (File.isValid(file) && Rank.isValid(rank)) {
					final int targetSquare = Square.onFileRank(file, rank);
					final int index = getItemIndex(beginSquare, targetSquare);
					table[index] = mask;

					mask |= BitBoard.getSquareMask(targetSquare);

					file += offset.getFileOffset();
					rank += offset.getRankOffset();
				}
			}
		}
		
		return table;
	}

	public static long getItem (final int square1, final int square2) {
		final int index = getItemIndex(square1, square2);
		
		return table[index];
	}
	
}
