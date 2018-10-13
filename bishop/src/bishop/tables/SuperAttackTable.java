package bishop.tables;

import bishop.base.PieceType;
import bishop.base.Square;

/**
 * Table that contains masks of squares attackable by any piece from given square.
 */
public class SuperAttackTable {

	public static long getItem (final int square) {
		return table[square];
	}

	private static final long[] table = createTable();

	private static long[] createTable() {
		final long[] table = new long[Square.LAST];

		for (int square = Square.FIRST; square < Square.LAST; square++)
			table[square] = FigureAttackTable.getItem(PieceType.QUEEN, square) | FigureAttackTable.getItem(PieceType.KNIGHT, square);

		return table;
	}

}
