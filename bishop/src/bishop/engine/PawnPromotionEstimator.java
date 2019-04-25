package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;
import bishop.tables.FigureAttackTable;

public class PawnPromotionEstimator {

	public static final String FEATURES_HEADER = "defendantInCheck,attackerInCheck,attackerPawnCount,defendantPawnCount,defendantPawnCountOneMoveToPromotion,defendantPawnCountOneMoveToPromotionWithCheck,defendantPawnCountTwoMovesToPromotion,queenProtected,queenAttacked";

	private boolean defendantInCheck;   // Defendant is in check
	private int attackerPawnCount;   // Count of attacker's pawns
	private int defendantPawnCount;   // Count of defendant's pawns
	private int defendantPawnCountOneMoveToPromotion;
	private int defendantPawnCountOneMoveToPromotionWithCheck;
	private int defendantPawnCountTwoMovesToPromotion;
	private boolean queenProtected;
	private boolean queenAttacked;

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

		// Squares that blocks defendant pawns
		final long seventhRankMask = BoardConstants.getRankMask(Rank.getAbsolute(Rank.R7, defendantColor));
		final long queenBlockedSquares = getQueenAttackedSquare(queenSquare, occupancy & ~seventhRankMask, occupancy);

		final long blockers = attackerPawns | defendantPawns |
				BitBoard.of(attackerKingSquare) |
				((FigureAttackTable.getItem(PieceType.KING, attackerKingSquare) | queenBlockedSquares) & ~FigureAttackTable.getItem(PieceType.KING, defendantKingSquare));

		final long blockedSquares = BitBoard.extendForwardByColorWithoutItself(attackerColor, blockers);   // Squares on which the attacker's pawns would be blocked

		final long notBlockedDefendantPawnsOnSevenRank = defendantPawns & seventhRankMask & ~blockedSquares;
		defendantPawnCountOneMoveToPromotion = BitBoard.getSquareCount(notBlockedDefendantPawnsOnSevenRank);

		final long checkingSquares = getQueenAttackedSquare(attackerKingSquare, occupancy & ~notBlockedDefendantPawnsOnSevenRank, occupancy);

		final long defendantPawnsOnSevenRankWithCheck = BoardConstants.getPawnSingleMoveSquares(defendantColor, notBlockedDefendantPawnsOnSevenRank) & checkingSquares;
		defendantPawnCountOneMoveToPromotionWithCheck = BitBoard.getSquareCount(defendantPawnsOnSevenRankWithCheck);

		final long notBlockedDefendantPawnsOnSixthRank = defendantPawns & BoardConstants.getRankMask(Rank.getAbsolute(Rank.R6, defendantColor)) & ~blockedSquares;
		defendantPawnCountTwoMovesToPromotion = BitBoard.getSquareCount(notBlockedDefendantPawnsOnSixthRank);

		final long kingMaskAroundQueen = FigureAttackTable.getItem(PieceType.KING, queenSquare);
		queenProtected = BitBoard.containsSquare(kingMaskAroundQueen, attackerKingSquare);
		queenAttacked = BitBoard.containsSquare(kingMaskAroundQueen, defendantKingSquare);
	}

	private long getQueenAttackedSquare(final int square, final long orthogonalBlockers, final long diagonalBlockers) {
		final int indexDiagonal = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, square, diagonalBlockers);
		final long attackDiagonal = LineAttackTable.getAttackMask(indexDiagonal);

		final int indexOrthogonal = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, square, orthogonalBlockers);
		final long attackOrthogonal = LineAttackTable.getAttackMask(indexOrthogonal);

		return attackOrthogonal | attackDiagonal;
	}

	public String getFeatures () {
		final StringBuilder result = new StringBuilder();

		result.append(defendantInCheck).append(",");
		result.append(attackerPawnCount).append(",");
		result.append(defendantPawnCount).append(",");
		result.append(defendantPawnCountOneMoveToPromotion).append(",");
		result.append(defendantPawnCountOneMoveToPromotionWithCheck).append(",");
		result.append(defendantPawnCountTwoMovesToPromotion).append(",");
		result.append(queenProtected).append(",");
		result.append(queenAttacked);

		return result.toString();
	}

	public int estimate() {
		final double zWin = 5.93153 +
				(defendantInCheck ? 5.25171 : 0.0) +
				2.00215 * attackerPawnCount +
				-0.08915 * defendantPawnCount +
				-7.21683 * defendantPawnCountOneMoveToPromotion +
				-2.04623 * defendantPawnCountOneMoveToPromotionWithCheck +
				-3.29355 * defendantPawnCountTwoMovesToPromotion +
				(queenProtected ? -0.09224 : 0.0) +
				(queenAttacked ? 6.15914 : 0.0);

		final double zLose = -10.7836 +
				(defendantInCheck ? -21.2530 : 0.0) +
				0.6665 * attackerPawnCount +
				1.8224 * defendantPawnCount +
				3.2597 * defendantPawnCountOneMoveToPromotion +
				3.1986 * defendantPawnCountOneMoveToPromotionWithCheck +
				0.5452 * defendantPawnCountTwoMovesToPromotion +
				(queenProtected ? -0.4086 : 0.0) +
				(queenAttacked ? 6.8980 : 0.0);

		if (zWin < 0 && zLose < 0)
			return Classification.DRAW;

		if (zWin > zLose)
			return Classification.LOSE;
		else
			return Classification.WIN;
	}
}
