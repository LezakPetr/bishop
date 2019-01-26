package bishop.engine;

import bishop.base.BitBoard;
import bishop.base.BoardConstants;
import bishop.base.Square;

public class MaxKingDistanceCalculator {

	private static final int DISTANCE_BITS = 3;
	private static final int MAX_DISTANCE = 1 << DISTANCE_BITS;
	private static final long[] TABLE = initializeTable();

	private static long[] initializeTable() {
		final long[] table = new long[Square.LAST << DISTANCE_BITS];

		for (int kingSquare = Square.FIRST; kingSquare < Square.LAST; kingSquare++) {
			for (int distance = 0; distance < MAX_DISTANCE; distance++) {
				long mask = BitBoard.EMPTY;

				for (int square = Square.FIRST; square < Square.LAST; square++) {
					if (BoardConstants.getKingSquareDistance(kingSquare, square) > distance)
						mask |= BitBoard.of(square);
				}

				table[(kingSquare << DISTANCE_BITS) + distance] = mask;
			}
		}

		return table;
	}

	public static int getMaxKingDistance (final int square, final long mask) {
		final int index = square << DISTANCE_BITS;
		int distance = 7;

		for (int i = DISTANCE_BITS - 1; i >= 0; i--) {
			int testDistance = distance & ~(1 << i);

			if ((TABLE[index + testDistance] & mask) == 0)
				distance = testDistance;
		}

		return distance;
	}
}
