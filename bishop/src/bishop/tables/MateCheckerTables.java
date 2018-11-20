package bishop.tables;

import bishop.base.*;

public class MateCheckerTables {
	private static final long[][] LINE_AFFECTING_SQUARES = createLineAffectingSquares();
	private static final long[] KNIGHT_AFFECTING_SQUARES = createKnightAffectingSquares();

	private static final long[][] createLineAffectingSquares() {
		final long[][] table = new long[CrossDirection.LAST][Square.LAST];

		for (int crossDirection = CrossDirection.FIRST; crossDirection < CrossDirection.LAST; crossDirection++) {
			for (int kingSquare = Square.FIRST; kingSquare < Square.LAST; kingSquare++) {
				long affectingSquare = BitBoard.EMPTY;

				for (BitLoop loop = new BitLoop(BoardConstants.getKingNearSquares(kingSquare)); loop.hasNextSquare(); ) {
					final int nearSquare = loop.getNextSquare();
					final int pieceType = (crossDirection == CrossDirection.ORTHOGONAL) ? PieceType.ROOK : PieceType.BISHOP;

					affectingSquare |= FigureAttackTable.getItem(pieceType, nearSquare);
				}

				table[crossDirection][kingSquare] = affectingSquare;
			}
		}

		return table;
	}

	private static final long[] createKnightAffectingSquares() {
		final long[] table = new long[Square.LAST];

		for (int kingSquare = Square.FIRST; kingSquare < Square.LAST; kingSquare++) {
			long affectingSquare = BitBoard.EMPTY;

			for (BitLoop loop = new BitLoop(BoardConstants.getKingNearSquares(kingSquare)); loop.hasNextSquare(); ) {
				final int nearSquare = loop.getNextSquare();

				affectingSquare |= FigureAttackTable.getItem(PieceType.KNIGHT, nearSquare);
			}

			table[kingSquare] = affectingSquare;
		}

		return table;
	}

	public static long getLineAffectingSquares (final int crossDirection, final int kingSquare) {
		return LINE_AFFECTING_SQUARES[crossDirection][kingSquare];
	}

	public static long getKnightAffectingSquares (final int kingSquare) {
		return KNIGHT_AFFECTING_SQUARES[kingSquare];
	}
}
