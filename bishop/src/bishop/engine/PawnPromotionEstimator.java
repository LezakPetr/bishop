package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;
import bishop.tables.FigureAttackTable;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
			"savedQueenCount";


	private boolean defendantInCheck;   // Defendant is in check
	private int attackerPawnCount;   // Count of attacker's pawns
	private int defendantPawnCount;   // Count of defendant's pawns
	private boolean attackerKingInMateRisk;
	private boolean defendantKingInMateRisk;
	private boolean queenProtected;
	private boolean queenAttacked;
	private int exchangeableQueenCount;
	private int capturableQueenCount;
	private int pawnTwoMovesToPromotionCount;
	private int savedQueenCount;



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

				final boolean queensExchangeable = promotedPawnAttackedByQueen && promotedPawnProtectedByKing && !promotedPawnAttackedByKing;
				final boolean queenCapturable = ((promotedPawnAttackedByQueen || promotedPawnAttackedByKing) && !promotedPawnProtectedByKing) ||
						(promotedPawnAttackedByQueen && promotedPawnAttackedByKing);

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
				if (!defendantInCheck && relativeRank == Rank.R6 && (BoardConstants.getSquaresInFrontExclusive(defendantColor, defendantPawnSquare) & occupancy) != 0 && BitBoard.containsSquare(kingMaskAroundPawn & BoardConstants.RANK_1278_MASK, defendantKingSquare))
					pawnTwoMovesToPromotionCount++;
			}
		}

		// Squares that blocks defendant pawns
		final long attackerKingTargetSquares = FigureAttackTable.getItem(PieceType.KING, attackerKingSquare);
		final long defendantKingTargetSquares = FigureAttackTable.getItem(PieceType.KING, defendantKingSquare);

		final long kingMaskAroundQueen = FigureAttackTable.getItem(PieceType.KING, queenSquare);
		queenProtected = BitBoard.containsSquare(kingMaskAroundQueen, attackerKingSquare);
		queenAttacked = BitBoard.containsSquare(kingMaskAroundQueen, defendantKingSquare);

		attackerKingInMateRisk = BitBoard.getSquareCount(attackerKingTargetSquares & ~defendantKingTargetSquares) <= 2;
		defendantKingInMateRisk = BitBoard.getSquareCount(defendantKingTargetSquares & ~attackerKingTargetSquares) <= 2;
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
		result.append(savedQueenCount);

		return result.toString();
	}

	public int estimate() {
		final double zWin = 5.80338 +
				(defendantInCheck ? 0.45304 : 0.0) +
				1.87848 * attackerPawnCount +
				-1.04330 * defendantPawnCount +
				(attackerKingInMateRisk ? 0.01922 : 0) +
				(defendantKingInMateRisk ? -0.04513 : 0) +
				(queenProtected ? -0.16769 : 0.0) +
				(queenAttacked ? 10.83873 : 0.0) +
				-5.92719 * exchangeableQueenCount +
				-1.04813 * capturableQueenCount +
				-1.61668 * pawnTwoMovesToPromotionCount +
				-6.77235 * savedQueenCount;

		final double zLose = -10.8128 +
				(defendantInCheck ? -2.0264 : 0.0) +
				0.3982 * attackerPawnCount +
				2.5046 * defendantPawnCount +
				(attackerKingInMateRisk ? 2.6463 : 0) +
				(defendantKingInMateRisk ? -0.3147 : 0) +
				(queenProtected ? 0.1203 : 0.0) +
				(queenAttacked ? -9.1303 : 0.0) +
				4.1121 * exchangeableQueenCount +
				1.4129 * capturableQueenCount +
				2.5993 * pawnTwoMovesToPromotionCount +
				3.8009 * savedQueenCount;

		if (zWin < 0 && zLose < 0)
			return Classification.DRAW;

		if (zWin > zLose)
			return Classification.LOSE;
		else
			return Classification.WIN;
	}
}
