package bishop.engine;

import bishop.base.*;
import bishop.tables.FigureAttackTable;

public class MobilityCalculator {
	private final long[] colorOccupancy = new long[Color.LAST];

	private final long[] knightMasks = new long[Color.LAST];
	private final long[] knightAttackedSquares = new long[Color.LAST];

	private final long[] bishopKeys = new long[Color.LAST];
	private final long[] bishopAttackedSquares = new long[Color.LAST];

	private final long[] rookKeys = new long[Color.LAST];
	private final long[] rookAttackedSquares = new long[Color.LAST];

	private final long[] queenKeys = new long[Color.LAST];
	private final long[] queenAttackedSquares = new long[Color.LAST];

	private final long[] attackedSquares = new long[Color.LAST];

	public void calculate(final Position position, final MobilityCalculator parentCalculator) {
		readColorOccupancy(position);

		final long changeMask = calculateChangeMask(parentCalculator);

		final long occupancy = position.getOccupancy();

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long blockingSquares = occupancy & ~position.getPiecesMask(oppositeColor, PieceType.KING);

			calculateKnights(color, position, parentCalculator, changeMask);
			calculateBishops(color, blockingSquares, position, parentCalculator, changeMask);
			calculateRooks(color, blockingSquares, position, parentCalculator, changeMask);
			calculateQueen(color, blockingSquares, position, parentCalculator, changeMask);
		}
	}

	private long calculateChangeMask(final MobilityCalculator parentCalculator) {
		if (parentCalculator == null)
			return BitBoard.FULL;
		else {
			return (colorOccupancy[Color.WHITE] ^ parentCalculator.colorOccupancy[Color.WHITE]) |
					(colorOccupancy[Color.BLACK] ^ parentCalculator.colorOccupancy[Color.BLACK]);
		}
	}

	public void calculate(final Position position) {
		calculate(position, null);
	}

	private void readColorOccupancy(final Position position) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			colorOccupancy[color] = position.getColorOccupancy(color);
		}
	}

	private void calculateKnights(final int color, final Position position, final MobilityCalculator parentCalculator, final long changeMask) {
		final long knightMask = position.getPiecesMask(color, PieceType.KNIGHT);

		if (parentCalculator != null && ((parentCalculator.knightMasks[color] | knightMask) & changeMask) == 0) {
			knightAttackedSquares[color] = parentCalculator.knightAttackedSquares[color];
			knightMasks[color] = parentCalculator.knightMasks[color];
		}
		else {
			long attackedSquares = BitBoard.EMPTY;

			for (BitLoop loop = new BitLoop(knightMask); loop.hasNextSquare(); ) {
				final int sourceSquare = loop.getNextSquare();
				final long attack = FigureAttackTable.getItem(PieceType.KNIGHT, sourceSquare);
				attackedSquares |= attack;
			}

			knightAttackedSquares[color] = attackedSquares;
			knightMasks[color] = knightMask;
		}
	}

	private void calculateBishops(final int color, final long blockingSquares, final Position position, final MobilityCalculator parentCalculator, final long changeMask) {
		final long bishopMask = position.getPiecesMask(color, PieceType.BISHOP);

		if (parentCalculator != null && ((parentCalculator.bishopKeys[color] | bishopMask) & changeMask) == 0) {
			bishopKeys[color] = parentCalculator.bishopKeys[color];
			bishopAttackedSquares[color] = parentCalculator.bishopAttackedSquares[color];
		}
		else {
			long attackedSquares = BitBoard.EMPTY;

			for (BitLoop loop = new BitLoop(bishopMask); loop.hasNextSquare(); ) {
				final int sourceSquare = loop.getNextSquare();

				final int index = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquares);
				final long attack = LineAttackTable.getAttackMask(index);
				attackedSquares |= attack;
			}

			bishopKeys[color] = bishopMask | attackedSquares;
			bishopAttackedSquares[color] = attackedSquares;
		}
	}

	private void calculateRooks(final int color, final long blockingSquares, final Position position, final MobilityCalculator parentCalculator, final long changeMask) {
		final long rookMask = position.getPiecesMask(color, PieceType.ROOK);

		if (parentCalculator != null && ((parentCalculator.rookKeys[color] | rookMask) & changeMask) == 0) {
			rookKeys[color] = parentCalculator.rookKeys[color];
			rookAttackedSquares[color] = parentCalculator.rookAttackedSquares[color];
		}
		else {
			long attackedSquares = BitBoard.EMPTY;

			for (BitLoop loop = new BitLoop(rookMask); loop.hasNextSquare(); ) {
				final int sourceSquare = loop.getNextSquare();

				final int index = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquares);
				final long attack = LineAttackTable.getAttackMask(index);
				attackedSquares |= attack;
			}

			rookKeys[color] = rookMask | attackedSquares;
			rookAttackedSquares[color] = attackedSquares;
		}
	}

	private void calculateQueen(final int color, final long blockingSquares, final Position position, final MobilityCalculator parentCalculator, final long changeMask) {
		final long queenMask = position.getPiecesMask(color, PieceType.QUEEN);

		if (parentCalculator != null && ((parentCalculator.queenKeys[color] | queenMask) & changeMask) == 0) {
			queenKeys[color] = parentCalculator.queenKeys[color];
			queenAttackedSquares[color] = parentCalculator.queenAttackedSquares[color];
		}
		else {
			long attackedSquares = BitBoard.EMPTY;

			for (BitLoop loop = new BitLoop(queenMask); loop.hasNextSquare(); ) {
				final int sourceSquare = loop.getNextSquare();

				final int indexDiagonal = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, sourceSquare, blockingSquares);
				final long attackDiagonal = LineAttackTable.getAttackMask(indexDiagonal);

				final int indexOrthogonal = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, sourceSquare, blockingSquares);
				final long attackOrthogonal = LineAttackTable.getAttackMask(indexOrthogonal);

				final long attack = attackOrthogonal | attackDiagonal;
				attackedSquares |= attack;
			}

			queenKeys[color] = queenMask | attackedSquares;
			queenAttackedSquares[color] = attackedSquares;
		}
	}

	public long getKnightAttackedSquares(final int color) {
		return knightAttackedSquares[color];
	}

	public long getBishopAttackedSquares(final int color) {
		return bishopAttackedSquares[color];
	}

	public long getRookAttackedSquares(final int color) {
		return rookAttackedSquares[color];
	}

	public long getQueenAttackedSquares(final int color) {
		return queenAttackedSquares[color];
	}

}
