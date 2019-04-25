package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;


/**
 * Evaluator of terminal positions in pawn ending.
 */
public class PawnEndingTerminalPositionEvaluator {

    private final PawnEndingKey key;
    private final long pawnOccupancy;
    private final long[] squaresAttackedByPawns;
    private final PawnEndingTableRegister register;
	private final PawnPromotionEstimator promotionEstimator = new PawnPromotionEstimator();


    public PawnEndingTerminalPositionEvaluator (final PawnEndingTableRegister register, final PawnEndingKey key) {
        this.register = register;
        this.key = key;
        this.pawnOccupancy = key.getPawnOccupancy();

        this.squaresAttackedByPawns = Color.mapToBitBoardArray(
                c -> BoardConstants.getPawnsAttackedSquares(c, key.getPawnMask(c))
        );
    }

    private int evaluateTerminalPosition(final int kingOnTurnSquare, final int kingNotOnTurnSquare, final int onTurn) {
        if (isIllegalPosition(kingOnTurnSquare, kingNotOnTurnSquare, onTurn))
            return Classification.ILLEGAL;

        if ((pawnOccupancy & BoardConstants.RANK_18_MASK) != 0)
            return estimatePromotion(kingOnTurnSquare, kingNotOnTurnSquare);

        return Classification.UNKNOWN;
    }

    private boolean isIllegalPosition(final int kingOnTurnSquare, final int kingNotOnTurnSquare, final int onTurn) {
        if (BoardConstants.getKingSquareDistance(kingNotOnTurnSquare, kingOnTurnSquare) <= 1)
            return true;   // Kings are attacking themselves or they are on same square

        final long kingOccupancy = BitBoard.of(kingOnTurnSquare, kingNotOnTurnSquare);

        if ((kingOccupancy & pawnOccupancy) != 0)
            return true;   // Some king is on square with pawn

        if (BitBoard.containsSquare(squaresAttackedByPawns[onTurn], kingNotOnTurnSquare))
            return true;   // King not on turn is attacked by pawn

        return false;
    }

    private int estimatePromotion(final int defendantKingSquare, final int attackerKingSquare) {
        final int onTurn = ((key.getWhitePawns() & BoardConstants.RANK_8_MASK) != 0) ? Color.BLACK : Color.WHITE;
        final int notOnTurn = Color.getOppositeColor(onTurn);
        final long promotedPawnMask = key.getPawnMask(notOnTurn) & BoardConstants.RANK_18_MASK;
        final int promotedPawnSquare = BitBoard.getFirstSquare(promotedPawnMask);

        final int defendantKingDistance = BoardConstants.getKingSquareDistance(defendantKingSquare, promotedPawnSquare);
        final int attackerKingDistance = BoardConstants.getKingSquareDistance(attackerKingSquare, promotedPawnSquare);

        if (defendantKingDistance <= 1) {
            // Promoted queen is attacked by king.
            if (attackerKingDistance <= 1) {
                // Promoted queen is protected by king. Side with queen wins.
                return Classification.LOSE;
            }
            else {
                // Promoted queen is not protected. We can assume that the queen is captured by king
                // and calculate resulting pawn ending.
                final PawnEndingKey subKey = new PawnEndingKey(key.getWhitePawns() & ~promotedPawnMask, key.getBlackPawns() & ~promotedPawnMask);
                final PawnEndingTable subTable = register.getTable(subKey);
                final int subClassification = subTable.getClassification(attackerKingSquare, promotedPawnSquare, notOnTurn);

                return Classification.getOpposite (subClassification);
            }
        }
        else {
            // Queen is not attacked
            return getNotAttackedQueenEvaluation(key, attackerKingSquare, defendantKingSquare, promotedPawnSquare, onTurn);
        }
    }

    // Evaluate position where queen is not attacked.
    private int getNotAttackedQueenEvaluation (final PawnEndingKey key, final int attackerKingSquare, final int defendantKingSquare, final int queenSquare, final int onTurn) {
    	promotionEstimator.init(key, attackerKingSquare, defendantKingSquare);

    	return promotionEstimator.estimate();
    }

    private PawnEndingTable createPrecalculatedTable(final int onTurn) {
        final long[] wonPositionsOnTurn = new long[Square.LAST];
        final long[] lostPositionsOnTurn = new long[Square.LAST];

        // King with the pawn (not on turn)
        for (int kingOnTurnSquare = Square.FIRST; kingOnTurnSquare < Square.LAST; kingOnTurnSquare++) {
            for (int kingNotOnTurnSquare = Square.FIRST; kingNotOnTurnSquare < Square.LAST; kingNotOnTurnSquare++) {
                final int classification = evaluateTerminalPosition(kingOnTurnSquare, kingNotOnTurnSquare, onTurn);

                switch (classification) {
                    case Classification.WIN:
                    case Classification.ILLEGAL:
                        wonPositionsOnTurn[kingNotOnTurnSquare] |= BitBoard.getSquareMask(kingOnTurnSquare);
                        break;

                    case Classification.LOSE:
                        lostPositionsOnTurn[kingOnTurnSquare] |= BitBoard.getSquareMask(kingNotOnTurnSquare);
                        break;
                }
            }
        }

        final long[][] wonPositions = new long[Color.LAST][];
        wonPositions[onTurn] = wonPositionsOnTurn;

        final long[][] lostPositions = new long[Color.LAST][];
        lostPositions[onTurn] = lostPositionsOnTurn;

        return new PawnEndingTable(wonPositions, lostPositions);
    }

    public PawnEndingTable calculateTable(final int onTurn) {
		return createPrecalculatedTable(onTurn);
    }
}
