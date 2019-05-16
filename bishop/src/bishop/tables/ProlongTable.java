package bishop.tables;

import bishop.base.*;

public class ProlongTable {

	private static final int BEGIN_SQUARE_SHIFT = 0;
	private static final int TARGET_SQUARE_SHIFT = BEGIN_SQUARE_SHIFT + Square.BIT_COUNT;
	private static final int BIT_COUNT = TARGET_SQUARE_SHIFT + Square.BIT_COUNT;

	private static final long[] table = createTable();

	private static int getItemIndex (final int beginSquare, final int targetSquare) {
		return beginSquare + (targetSquare << TARGET_SQUARE_SHIFT);
	}

	private static long[] createTable() {
		final int size = 1 << BIT_COUNT;
		final long[] table = new long[size];

		final int figure = PieceType.QUEEN;
		final int directionCount = FigureMoveOffsets.getFigureDirectionCount (figure);

		for (int beginSquare = Square.FIRST; beginSquare < Square.LAST; beginSquare++) {
			for (int direction = 0; direction < directionCount; direction++) {
				final FileRankOffset offset = FigureMoveOffsets.getFigureOffset (figure, direction);

				int file = Square.getFile(beginSquare);
				int rank = Square.getRank(beginSquare);

				do {
					file += offset.getFileOffset();
					rank += offset.getRankOffset();
				} while (File.isValid(file) && Rank.isValid(rank));

				long mask = 0;
				int targetSquare;

				while (true) {
					file -= offset.getFileOffset();
					rank -= offset.getRankOffset();

					targetSquare = Square.onFileRank(file, rank);

					if (targetSquare == beginSquare)
						break;

					table[getItemIndex(beginSquare, targetSquare)] = mask;
					mask |= BitBoard.of(targetSquare);
				}
			}
		}

		return table;
	}

	public static long getItem (final int beginSquare, final int targetSquare) {
		final int index = getItemIndex(beginSquare, targetSquare);

		return table[index];
	}

}
