package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;
import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.ProlongTable;
import utils.LongArrayBuilder;

public class PawnPromotionEstimator {

	private static final long[] SIXTH_RANK_MASK = LongArrayBuilder.create(Color.LAST)
			.put(Color.WHITE, BoardConstants.RANK_6_MASK)
			.put(Color.BLACK, BoardConstants.RANK_3_MASK)
			.build();

	private static final long[] SEVENTH_RANK_MASK = LongArrayBuilder.create(Color.LAST)
			.put(Color.WHITE, BoardConstants.RANK_7_MASK)
			.put(Color.BLACK, BoardConstants.RANK_2_MASK)
			.build();

	private static final long[][] PAWN_TWO_MOVES_TO_PROMOTION_MASK = initPawnTwoMovesToPromotionMask();

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
			"normalPawnTwoMovesToPromotionCount," +
			"stalematingPawnTwoMovesToPromotionCount," +
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
	int normalPawnTwoMovesToPromotionCount;
	int stalematingPawnTwoMovesToPromotionCount;
	int savedQueenCount;
	int attackerPawnOnSevenRankCount;

	private static long[][] initPawnTwoMovesToPromotionMask() {
		final long[][] result = new long[Color.LAST][Square.LAST];

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int defendantKingSquare = Square.FIRST; defendantKingSquare < Square.LAST; defendantKingSquare++) {
				for (int pawnSquare = Square.FIRST_PAWN_SQUARE; pawnSquare < Square.LAST_PAWN_SQUARE; pawnSquare++) {
					final boolean isPawnTwoMovesToPromotion =
							Rank.getRelative(Square.getRank(defendantKingSquare), color) >= Rank.R6 &&
							Math.abs(Square.getFile(defendantKingSquare) - Square.getFile(pawnSquare)) <= 1 &&
							Rank.getRelative(Square.getRank(pawnSquare), color) == Rank.R6;

					if (isPawnTwoMovesToPromotion)
						result[color][defendantKingSquare] |= BitBoard.of(pawnSquare);
				}
			}
		}

		return result;
	}

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
		savedQueenCount = 0;

		final long defendantKingAttackedSquares = FigureAttackTable.getItem(PieceType.KING, defendantKingSquare);

		if (!defendantInCheck) {
			final long pawnsOnSeventhRank = defendantPawns & SEVENTH_RANK_MASK[defendantColor];

			for (BitLoop defendantPawnLoop = new BitLoop(pawnsOnSeventhRank); defendantPawnLoop.hasNextSquare(); ) {
				final int defendantPawnSquare = defendantPawnLoop.getNextSquare();
				final int promotionSquare = BoardConstants.getPawnPromotionSquare(defendantColor, defendantPawnSquare);

				if (attackerKingSquare != promotionSquare && defendantKingSquare != promotionSquare) {
					// Pawn can be promoted
					final long kingMaskAroundPawn = FigureAttackTable.getItem(PieceType.KING, promotionSquare);
					final long squaresAttackedByPromotedPawn = getQueenAttackedSquare(promotionSquare, occupancy & ~BitBoard.of(defendantPawnSquare), occupancy);
					final boolean attackerWouldBeInCheck = BitBoard.containsSquare(squaresAttackedByPromotedPawn, attackerKingSquare);

					final boolean promotedPawnAttackedByQueen = BitBoard.containsSquare(squaresAttackedByPromotedPawn, queenSquare);
					final boolean promotedPawnAttackedByKing = BitBoard.containsSquare(kingMaskAroundPawn, attackerKingSquare);
					final boolean promotedPawnProtectedByKing = BitBoard.containsSquare(kingMaskAroundPawn, defendantKingSquare);

					final long pinSquares = ProlongTable.getItem(promotionSquare, defendantKingSquare) &
							getQueenAttackedSquare(defendantKingSquare, occupancy, occupancy) &
							~attackerPawns & ~BitBoard.of(attackerKingSquare) &
							~defendantKingAttackedSquares;

					final boolean queenPinnable = !attackerWouldBeInCheck &&
							BitBoard.containsSquare(squaresAttackedByPromotedPawn, defendantKingSquare) &&
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
			}
		}

		if (!defendantInCheck) {
			final long blockedSquares = BitBoard.extendForwardByColorWithoutItself(attackerColor, occupancy);
			final long pawnsTwoMovesToPromotion = defendantPawns &
					PAWN_TWO_MOVES_TO_PROMOTION_MASK[defendantColor][defendantKingSquare] &
					~blockedSquares;

			if (defendantPawnCount == 1) {
				stalematingPawnTwoMovesToPromotionCount = BitBoard.getSquareCountSparse(pawnsTwoMovesToPromotion & BoardConstants.FILE_ACFH_MASK);
				normalPawnTwoMovesToPromotionCount = BitBoard.getSquareCountSparse(pawnsTwoMovesToPromotion & ~BoardConstants.FILE_ACFH_MASK);
			}
			else {
				stalematingPawnTwoMovesToPromotionCount = 0;
				normalPawnTwoMovesToPromotionCount = BitBoard.getSquareCountSparse(pawnsTwoMovesToPromotion);
			}
		}

		attackerPawnOnSevenRankCount = BitBoard.getSquareCountSparse(attackerPawns & SEVENTH_RANK_MASK[attackerColor]);

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
		result.append(normalPawnTwoMovesToPromotionCount).append(",");
		result.append(stalematingPawnTwoMovesToPromotionCount).append(",");
		result.append(savedQueenCount).append(",");;
		result.append(attackerPawnOnSevenRankCount);

		return result.toString();
	}

	public int estimate() {
		final double zWin = 6.08236 +
				(defendantInCheck ? 0.14016 : 0.0) +
				2.48287 * attackerPawnCount +
				-1.04147 * defendantPawnCount +
				(attackerKingInMateRisk ? 0.25733 : 0) +
				(defendantKingInMateRisk ? 0.27758 : 0) +
				(queenProtected ? -0.05739 : 0.0) +
				(queenAttacked ? 10.64491 : 0.0) +
				-6.44507 * exchangeableQueenCount +
				-1.71240 * capturableQueenCount +
				-2.33803 * normalPawnTwoMovesToPromotionCount +
				-5.08712 * stalematingPawnTwoMovesToPromotionCount +
				-8.22822 * savedQueenCount +
				1.54418 * attackerPawnOnSevenRankCount;

		final double zLose = -10.897410 +
				(defendantInCheck ? -1.943260 : 0.0) +
				0.457819 * attackerPawnCount +
				2.518333 * defendantPawnCount +
				(attackerKingInMateRisk ? 2.598186 : 0) +
				(defendantKingInMateRisk ? -0.497930 : 0) +
				(queenProtected ? -0.003037 : 0.0) +
				(queenAttacked ? -9.015048 : 0.0) +
				2.748906 * exchangeableQueenCount +
				1.458396 * capturableQueenCount +
				0.978398 * normalPawnTwoMovesToPromotionCount +
				-10.437299 * stalematingPawnTwoMovesToPromotionCount +
				4.034489 * savedQueenCount +
				-0.397108 * attackerPawnOnSevenRankCount;

		if (zWin < 0 && zLose < 0)
			return Classification.DRAW;

		if (zWin > zLose)
			return Classification.LOSE;
		else
			return Classification.WIN;
	}
}
