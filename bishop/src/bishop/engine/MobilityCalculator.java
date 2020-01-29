package bishop.engine;

import bishop.base.*;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;

import java.util.Arrays;

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

	private final long[] pawnAttackedSquares = new long[Color.LAST];
	private final long[] kingAttackedSquares = new long[Color.LAST];

	private final long[] attackedSquares = new long[Color.LAST];

	public void calculate(final Position position, final MobilityCalculator parentCalculator) {
		readColorOccupancy(position);

		final long changeMask = calculateChangeMask(parentCalculator);
		final long occupancy = position.getOccupancy();

		Arrays.fill(attackedSquares, BitBoard.EMPTY);

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final long blockingSquares = occupancy & ~position.getPiecesMask(oppositeColor, PieceType.KING);

			calculateKnights(color, position, parentCalculator, changeMask);
			calculateBishops(color, blockingSquares, position, parentCalculator, changeMask);
			calculateRooks(color, blockingSquares, position, parentCalculator, changeMask);
			calculateQueens(color, blockingSquares, position, parentCalculator, changeMask);
			calculatePawns(color, position);
			calculateKings(color, position);
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

		if (parentCalculator != null && ((parentCalculator.knightMasks[color] | knightMask) & changeMask) == 0)
			knightAttackedSquares[color] = parentCalculator.knightAttackedSquares[color];
		else {
			long attackedSquares = BitBoard.EMPTY;

			for (BitLoop loop = new BitLoop(knightMask); loop.hasNextSquare(); ) {
				final int sourceSquare = loop.getNextSquare();
				final long attack = FigureAttackTable.getItem(PieceType.KNIGHT, sourceSquare);
				attackedSquares |= attack;
			}

			knightAttackedSquares[color] = attackedSquares;
		}

		knightMasks[color] = knightMask;
		attackedSquares[color] |= knightAttackedSquares[color];
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

		attackedSquares[color] |= bishopAttackedSquares[color];
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

		attackedSquares[color] |= rookAttackedSquares[color];
	}

	private void calculateQueens(final int color, final long blockingSquares, final Position position, final MobilityCalculator parentCalculator, final long changeMask) {
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

		attackedSquares[color] |= queenAttackedSquares[color];
	}

	private void calculatePawns(final int color, final Position position) {
		final long attack = BoardConstants.getPawnsAttackedSquares(color, position.getPiecesMask(color, PieceType.PAWN));
		pawnAttackedSquares[color] = attack;
		attackedSquares[color] |= attack;
	}

	private void calculateKings(final int color, final Position position) {
		final long attack = FigureAttackTable.getItem(PieceType.KING, position.getKingPosition(color));
		kingAttackedSquares[color] = attack;
		attackedSquares[color] |= attack;
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

	public long getPawnAttackedSquares(final int color) {
		return pawnAttackedSquares[color];
	}

	public long getAllAttackedSquares(final int color) {
		return attackedSquares[color];
	}

	public boolean isSquareAttacked (final int color, final int square) {
		return BitBoard.containsSquare(attackedSquares[color], square);
	}

	/**
	 * Tests if there is a double check.
	 * This method assumes that the position preceding this position was legal (without king not on turn in check). This implies
	 * that the two checking pieces must have different type. One of checking pieces must be sliding piece and this one
	 * had to be blocked by piece of different type (otherwise that piece would be on the line of blocked piece and thus would check).
	 * This method works only for testing double checks, not other double attacks.
	 * @param color color of checking pieces
	 * @param kingSquare king square
	 * @return true if there is a double check, false if not
	 */
	public boolean isDoubleCheck(final int color, final int kingSquare) {
		final long squareMask = BitBoard.of(kingSquare);

		if ((attackedSquares[color] & squareMask) == 0)
			return false;   // No check

		// Now we know that there is one or two different checking pieces. To distinguish these situations we just XOR all
		// attack masks. If there is a single check the result will have 1 on the king square, if there is a double check there will be 0.
		final long xoredMask =
				pawnAttackedSquares[color] ^
				knightAttackedSquares[color] ^
				bishopAttackedSquares[color] ^
				rookAttackedSquares[color] ^
				queenAttackedSquares[color] ^
				kingAttackedSquares[color];

		return (xoredMask & squareMask) == 0;
	}

	public boolean canBeMate(final Position position) {
		final int onTurn = position.getOnTurn();
		final int notOnTurn = Color.getOppositeColor(onTurn);
		final int kingSquare = position.getKingPosition(onTurn);
		final long kingMask = BitBoard.getSquareMask(kingSquare);

		final long requiredSquares = kingMask | FigureAttackTable.getItem(PieceType.KING, kingSquare);
		final long inaccessibleSquares = (position.getColorOccupancy(onTurn) & ~kingMask) | attackedSquares[notOnTurn];

		return ((~inaccessibleSquares & requiredSquares) == 0);
	}

	public boolean isStablePosition (final Position position) {
		final int onTurn = position.getOnTurn();
		final int oppositeColor = Color.getOppositeColor(onTurn);

		final long ownPawns = position.getPiecesMask(onTurn, PieceType.PAWN);
		final long ownFigures = position.getColorOccupancy(onTurn) & ~ownPawns;
		final long unprotectedPawns = ownPawns & ~attackedSquares[onTurn];
		final long attacks = attackedSquares[oppositeColor];

		return (attacks & (ownFigures | unprotectedPawns)) == 0;
	}

	public long getWinningSquares(final Position position) {
		final int onTurn = position.getOnTurn();
		final int oppositeColor = Color.getOppositeColor(onTurn);
		final long attacks = attackedSquares[onTurn];
		final long defenses = attackedSquares[oppositeColor];

		return attacks & ~defenses & position.getColorOccupancy(oppositeColor);
	}

	/**
	 * This method expects that the king on turn is in single check.
	 * The method returns true if the checking piece can be captured and is not protected.
	 * @param position position
	 * @return true if the checking piece can be captured and is not protected
	 */
	public boolean isSingleCheckWinning(final Position position) {
		final long winningSquares = getWinningSquares(position);
		final int onTurn = position.getOnTurn();
		final int oppositeColor = Color.getOppositeColor(onTurn);
		final int kingSquare = position.getKingPosition(onTurn);
		final long kingSquareMask = BitBoard.getSquareMask(kingSquare);

		final long rookAttacks = rookAttackedSquares[oppositeColor];
		final long bishopAttacks = bishopAttackedSquares[oppositeColor];
		final long queenAttacks = queenAttackedSquares[oppositeColor];

		final long longMovePieceMask =
				rookAttacks |
				bishopAttacks |
				queenAttacks;

		if ((longMovePieceMask & kingSquareMask) == 0) {
			// The check is from short move piece - pawn or knight
			if ((pawnAttackedSquares[oppositeColor] & kingSquareMask) != 0)
				return (position.getPiecesMask(oppositeColor, PieceType.PAWN) & PawnAttackTable.getItem(onTurn, kingSquare) & winningSquares) != 0;
			else
				return (position.getPiecesMask(oppositeColor, PieceType.KNIGHT) & FigureAttackTable.getItem (PieceType.KNIGHT, kingSquare) & winningSquares) != 0;
		}
		else {
			// The check is from long move piece - queen, rook or bishop
			final long occupancy = position.getOccupancy();

			// Verify check from orthogonal direction
			if (((rookAttacks | queenAttacks) & kingSquareMask) != 0) {
				final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, kingSquare, occupancy);
				final long orthogonalMask = LineAttackTable.getAttackMask(orthogonalIndex);

				final long orthogonalPieces =
						position.getPiecesMask(oppositeColor, PieceType.ROOK) |
						position.getPiecesMask(oppositeColor, PieceType.QUEEN);

				final long orthogonalCheckingPieces = orthogonalPieces & orthogonalMask;

				if (orthogonalCheckingPieces != 0)
					return (orthogonalCheckingPieces & winningSquares) != 0;
			}

			// The check must be from diagonal direction
			final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, kingSquare, occupancy);
			final long diagonalMask = LineAttackTable.getAttackMask(diagonalIndex);

			final long diagonalPieces =
					position.getPiecesMask(oppositeColor, PieceType.BISHOP) |
					position.getPiecesMask(oppositeColor, PieceType.QUEEN);

			return (diagonalPieces & diagonalMask & winningSquares) != 0;
		}
	}

}
