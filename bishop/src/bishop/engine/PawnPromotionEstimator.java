package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;
import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.ProlongTable;

public class PawnPromotionEstimator {

	public static final String FEATURES_HEADER =
			"defendantInCheck," +
			"attackerPawnCount," +
			"defendantPawnCount," +
			"attackerKingInMateRisk," +
			"defendantKingInMateRisk," +
			"queenProtected," +
			"queenAttacked," +
			"exchangeableQueenCount," +
			"capturableQueenCount," +
			"pawnTwoMovesToPromotionCount," +
			"savedQueenCount," +
			"attackerPawnOnSevenRankCount";


	boolean defendantInCheck;   // Defendant is in check
	int attackerPawnCount;   // Count of attacker's pawns
	int defendantPawnCount;   // Count of defendant's pawns
	boolean attackerKingInMateRisk;
	boolean defendantKingInMateRisk;
	boolean queenProtected;
	boolean queenAttacked;
	int exchangeableQueenCount;
	int capturableQueenCount;
	int pawnTwoMovesToPromotionCount;
	int savedQueenCount;
	int attackerPawnOnSevenRankCount;

	private long getQueenAttackedSquare(final int square, final long orthogonalBlockers, final long diagonalBlockers) {
		final int indexDiagonal = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, square, diagonalBlockers);
		final long attackDiagonal = LineAttackTable.getAttackMask(indexDiagonal);

		final int indexOrthogonal = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, square, orthogonalBlockers);
		final long attackOrthogonal = LineAttackTable.getAttackMask(indexOrthogonal);

		return attackOrthogonal | attackDiagonal;
	}

	public void init(final PawnEndingKey key, final int attackerKingSquare, final int defendantKingSquare) {
		final long pawnQueenOccupancy = key.getPawnOccupancy();   // Squares occupied by pawns and queens
		final long occupancy = pawnQueenOccupancy | BitBoard.of(attackerKingSquare, defendantKingSquare);   // All occupied squares
		final int defendantColor = ((pawnQueenOccupancy & BoardConstants.RANK_8_MASK) != 0) ? Color.BLACK : Color.WHITE;   // Color of defendant - side without queen
		final int attackerColor = Color.getOppositeColor(defendantColor);   // Color of the attacker - side with the queen
		final int queenSquare = BitBoard.getFirstSquare(pawnQueenOccupancy & BoardConstants.RANK_18_MASK);   // Square with (the only) queen

		final long squaresAttackedByQueen = getQueenAttackedSquare(queenSquare, occupancy, occupancy);   // Mask of squares attacked by the only queen
		defendantInCheck = BitBoard.containsSquare(squaresAttackedByQueen, defendantKingSquare);

		final long attackerPawns = key.getPawnMask(attackerColor) & BoardConstants.PAWN_ALLOWED_SQUARES;   // Mask of attacker's pawns
		attackerPawnCount = BitBoard.getSquareCount(attackerPawns);

		final long defendantPawns = key.getPawnMask(defendantColor) & BoardConstants.PAWN_ALLOWED_SQUARES;   // Mask of defendant's pawns
		defendantPawnCount = BitBoard.getSquareCount(defendantPawns);

		exchangeableQueenCount = 0;
		capturableQueenCount = 0;
		pawnTwoMovesToPromotionCount = 0;
		savedQueenCount = 0;

		final long defendantKingAttackedSquares = FigureAttackTable.getItem(PieceType.KING, defendantKingSquare);

		for (BitLoop defendantPawnLoop = new BitLoop(defendantPawns); defendantPawnLoop.hasNextSquare(); ) {
			final int defendantPawnSquare = defendantPawnLoop.getNextSquare();
			final int relativeRank = Rank.getRelative(Square.getRank(defendantPawnSquare), defendantColor);
			final int promotionSquare = BoardConstants.getPawnPromotionSquare(defendantColor, defendantPawnSquare);

			final long kingMaskAroundPawn = FigureAttackTable.getItem(PieceType.KING, promotionSquare);

			if (!defendantInCheck && relativeRank == Rank.R7 && attackerKingSquare != promotionSquare && defendantKingSquare != promotionSquare) {
				// Pawn can be promoted
				final long squaresAttackedByPromotedPawn = getQueenAttackedSquare(promotionSquare, occupancy & ~BitBoard.of(defendantPawnSquare), occupancy);

				final boolean promotedPawnAttackedByQueen = BitBoard.containsSquare(squaresAttackedByPromotedPawn, queenSquare);
				final boolean promotedPawnAttackedByKing = BitBoard.containsSquare(kingMaskAroundPawn, attackerKingSquare);
				final boolean promotedPawnProtectedByKing = BitBoard.containsSquare(kingMaskAroundPawn, defendantKingSquare);

				final long pinSquares = ProlongTable.getItem(promotionSquare, defendantKingSquare) &
						getQueenAttackedSquare(defendantKingSquare, occupancy, occupancy) &
						~attackerPawns & ~BitBoard.of(attackerKingSquare) &
						~defendantKingAttackedSquares;

				final boolean queenPinnable = BitBoard.containsSquare(squaresAttackedByPromotedPawn, defendantKingSquare) &&
						(squaresAttackedByQueen & pinSquares) != 0;

				final boolean queenPinnableWithoutProtection = queenPinnable &&
						(kingMaskAroundPawn & defendantKingAttackedSquares & ~BetweenTable.getItem(promotionSquare, defendantKingSquare)) == 0;

				final boolean queensExchangeable = (promotedPawnAttackedByQueen && promotedPawnProtectedByKing && !promotedPawnAttackedByKing) || (queenPinnable && !queenPinnableWithoutProtection);
				final boolean queenCapturable = ((promotedPawnAttackedByQueen || promotedPawnAttackedByKing) && !promotedPawnProtectedByKing) ||
						(promotedPawnAttackedByQueen && promotedPawnAttackedByKing) ||
						queenPinnableWithoutProtection;

				if (queensExchangeable)
					exchangeableQueenCount++;
				else {
					if (queenCapturable)
						capturableQueenCount++;
					else
						savedQueenCount++;
				}
			}
			else {
				// Pawn cannot be promoted
				if (!defendantInCheck && relativeRank == Rank.R6 && (BoardConstants.getSquaresInFrontExclusive(defendantColor, defendantPawnSquare) & occupancy) == 0 && BitBoard.containsSquare(kingMaskAroundPawn & BoardConstants.RANK_1278_MASK, defendantKingSquare))
					pawnTwoMovesToPromotionCount++;
			}
		}

		attackerPawnOnSevenRankCount = BitBoard.getSquareCount(
				attackerPawns & BoardConstants.getRankMask(Rank.getAbsolute(Rank.R7, attackerColor))
		);

		// Squares that blocks defendant pawns
		final long attackerKingTargetSquares = FigureAttackTable.getItem(PieceType.KING, attackerKingSquare);

		final long kingMaskAroundQueen = FigureAttackTable.getItem(PieceType.KING, queenSquare);
		queenProtected = BitBoard.containsSquare(kingMaskAroundQueen, attackerKingSquare);
		queenAttacked = BitBoard.containsSquare(kingMaskAroundQueen, defendantKingSquare);

		attackerKingInMateRisk = BitBoard.getSquareCount(attackerKingTargetSquares & ~defendantKingAttackedSquares) <= 2;
		defendantKingInMateRisk = BitBoard.getSquareCount(defendantKingAttackedSquares & ~attackerKingTargetSquares) <= 2;
	}

	public String getFeatures () {
		final StringBuilder result = new StringBuilder();

		result.append(defendantInCheck).append(",");
		result.append(attackerPawnCount).append(",");
		result.append(defendantPawnCount).append(",");
		result.append(attackerKingInMateRisk).append(",");
		result.append(defendantKingInMateRisk).append(",");
		result.append(queenProtected).append(",");
		result.append(queenAttacked).append(",");
		result.append(exchangeableQueenCount).append(",");
		result.append(capturableQueenCount).append(",");
		result.append(pawnTwoMovesToPromotionCount).append(",");
		result.append(savedQueenCount).append(",");;
		result.append(attackerPawnOnSevenRankCount);

		return result.toString();
	}

	public int estimate() {
		final double zWin = 5.91191 +
				(defendantInCheck ? 0.15723 : 0.0) +
				2.21205 * attackerPawnCount +
				-0.89213 * defendantPawnCount +
				(attackerKingInMateRisk ? 0.05764 : 0) +
				(defendantKingInMateRisk ? 0.31546 : 0) +
				(queenProtected ? -0.03113 : 0.0) +
				(queenAttacked ? 10.63715 : 0.0) +
				-6.56015 * exchangeableQueenCount +
				-2.80828 * capturableQueenCount +
				-3.78363 * pawnTwoMovesToPromotionCount +
				-7.95718 * savedQueenCount +
				1.49808 * attackerPawnOnSevenRankCount;

		final double zLose = -10.69381 +
				(defendantInCheck ? -2.16229 : 0.0) +
				0.46413 * attackerPawnCount +
				2.52537 * defendantPawnCount +
				(attackerKingInMateRisk ? 2.59111 : 0) +
				(defendantKingInMateRisk ? -0.48017 : 0) +
				(queenProtected ? 0.04962 : 0.0) +
				(queenAttacked ? -9.06008 : 0.0) +
				3.18915 * exchangeableQueenCount +
				2.06947 * capturableQueenCount +
				0.74375 * pawnTwoMovesToPromotionCount +
				3.73932 * savedQueenCount +
				-0.37207 * attackerPawnOnSevenRankCount;

		if (zWin < 0 && zLose < 0)
			return Classification.DRAW;

		if (zWin > zLose)
			return Classification.LOSE;
		else
			return Classification.WIN;
	}
}
