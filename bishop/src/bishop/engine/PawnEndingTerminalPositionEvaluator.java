package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;
import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;

public class PawnEndingTerminalPositionEvaluator {

    private final PawnEndingKey key;
    private final long pawnOccupancy;
    private final long[] squaresAttackedByPawns;
    private final TablebasePositionEvaluator tablebaseEvaluator;
    private final PawnEndingTableRegister register;
    private boolean useTablebase;

    private static final long NON_DEFENDABLE_MATE_MASK_WHITE =
            BoardConstants.RANK_1_MASK |
            BoardConstants.FILE_A_MASK |
            BoardConstants.FILE_H_MASK;
            //BitBoard.of(Square.A2, Square.H2);

    private static final long[] NON_DEFENDABLE_MATE_MASKS = {
            NON_DEFENDABLE_MATE_MASK_WHITE,
            BitBoard.getMirrorBoard(NON_DEFENDABLE_MATE_MASK_WHITE)
    };

    public PawnEndingTerminalPositionEvaluator (final TablebasePositionEvaluator tablebaseEvaluator, final PawnEndingTableRegister register, final PawnEndingKey key) {
        this.tablebaseEvaluator = tablebaseEvaluator;
        this.register = register;
        this.key = key;
        this.pawnOccupancy = key.getPawnOccupancy();
        this.useTablebase = (tablebaseEvaluator != null);

        this.squaresAttackedByPawns = Color.mapToBitBoardArray(
                c -> BoardConstants.getPawnsAttackedSquares(c, key.getPawnMask(c))
        );
    }

    public void setUseTablebase (final boolean use) {
        this.useTablebase = use;
    }

    public int evaluateTerminalPosition(final int kingOnTurnSquare, final int kingNotOnTurnSquare, final int onTurn) {
        if (isIllegalPosition(kingOnTurnSquare, kingNotOnTurnSquare, onTurn))
            return Classification.ILLEGAL;

        if ((pawnOccupancy & BoardConstants.RANK_18_MASK) != 0)
            return estimatePromotion(kingOnTurnSquare, kingNotOnTurnSquare);

        return Classification.UNKNOWN;
    }

    public boolean isIllegalPosition(final int kingOnTurnSquare, final int kingNotOnTurnSquare, final int onTurn) {
        if (BoardConstants.getKingSquareDistance(kingNotOnTurnSquare, kingOnTurnSquare) <= 1)
            return true;   // Kings are attacking themselves or they are on same square

        final long kingOccupancy = BitBoard.of(kingOnTurnSquare, kingNotOnTurnSquare);

        if ((kingOccupancy & pawnOccupancy) != 0)
            return true;   // Some king is on square with pawn

        if (BitBoard.containsSquare(squaresAttackedByPawns[onTurn], kingNotOnTurnSquare))
            return true;   // King not on turn is attacked by pawn

        return false;
    }

    public int estimatePromotion(final int defendantKingSquare, final int attackerKingSquare) {
        final int onTurn = ((key.getWhitePawns() & BoardConstants.RANK_8_MASK) != 0) ? Color.BLACK : Color.WHITE;
        final int notOnTurn = Color.getOppositeColor(onTurn);
        final long promotedPawnMask = key.getPawnMask(notOnTurn) & BoardConstants.RANK_18_MASK;
        final int promotedPawnSquare = BitBoard.getFirstSquare(promotedPawnMask);

        final int defendantKingDistance = BoardConstants.getKingSquareDistance(defendantKingSquare, promotedPawnSquare);
        final int attackerKingDistance = BoardConstants.getKingSquareDistance(attackerKingSquare, promotedPawnSquare);

        if (defendantKingDistance <= 1) {
            if (attackerKingDistance <= 1) {
                // Side with queen wins
                return Classification.LOSE;
            }
            else {
                final PawnEndingKey subKey = new PawnEndingKey(key.getWhitePawns() & ~promotedPawnMask, key.getBlackPawns() & ~promotedPawnMask);
                final PawnEndingTable subTable = register.getTable(subKey);
                final int subClassification = subTable.getClassification(attackerKingSquare, promotedPawnSquare, notOnTurn);

                return Classification.getOpposite (subClassification);
            }
        }
        else
            return getNotAttackedQueenEvaluation(key, attackerKingSquare, defendantKingSquare, promotedPawnSquare, onTurn);
    }

    private int getNotAttackedQueenEvaluation (final PawnEndingKey key, final int attackerKingSquare, final int defendantKingSquare, final int queenSquare, final int onTurn) {
        final long pawnOnSevenRankMask = key.getPawnMask(onTurn) & BoardConstants.getRankMask(Rank.getAbsolute(Rank.R7, onTurn));

        if (pawnOnSevenRankMask == BitBoard.EMPTY)
            return Classification.LOSE;   // No opposite pawn on the seventh rank - queen wins

        if (BitBoard.getSquareCount(pawnOnSevenRankMask) > 1)
            return Classification.DRAW;   // More pawns on the seventh rank - draw

        final long promotedPawnMask = key.getPawnOccupancy() & ~BoardConstants.PAWN_ALLOWED_SQUARES;
        final long mask = pawnOnSevenRankMask | promotedPawnMask;
        final long whitePawnMask = key.getWhitePawns() & mask;
        final long blackPawnMask = key.getBlackPawns() & mask;
        final PawnEndingKey subKey = new PawnEndingKey(whitePawnMask, blackPawnMask);
        final PawnEndingTable subTable = register.getTable(subKey);

        return subTable.getClassification(defendantKingSquare, attackerKingSquare, onTurn);
    }

    private static long getQueenAttackedSquares (final int square, final long blockers) {
        final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, square, blockers);
        final long orthogonalAttacks = LineAttackTable.getAttackMask(orthogonalIndex);

        final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, square, blockers);
        final long diagonalAttacks = LineAttackTable.getAttackMask(diagonalIndex);

        return orthogonalAttacks | diagonalAttacks;
    }

    private PawnEndingTable createPrecalculatedTable() {
        final long[][] wonPositions = new long[Color.LAST][Square.LAST];
        final long[][] lostPositions = new long[Color.LAST][Square.LAST];

        // King with the pawn (not on turn)
        for (int kingOnTurnSquare = Square.FIRST; kingOnTurnSquare < Square.LAST; kingOnTurnSquare++) {
            for (int kingNotOnTurnSquare = Square.FIRST; kingNotOnTurnSquare < Square.LAST; kingNotOnTurnSquare++) {
                for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
                    final int classification = evaluateTerminalPosition(kingOnTurnSquare, kingNotOnTurnSquare, onTurn);

                    switch (classification) {
                        case Classification.WIN:
                        case Classification.ILLEGAL:
                            wonPositions[onTurn][kingNotOnTurnSquare] |= BitBoard.getSquareMask(kingOnTurnSquare);
                            break;

                        case Classification.LOSE:
                            lostPositions[onTurn][kingOnTurnSquare] |= BitBoard.getSquareMask(kingNotOnTurnSquare);
                            break;
                    }
                }
            }
        }

        return new PawnEndingTable(wonPositions, lostPositions);
    }

    public PawnEndingTable calculateTable() {
        if (useTablebase && tablebaseEvaluator.canEvaluateMaterial(key.getMaterialHash()))
            return new TablebasePawnEndingTable(key, tablebaseEvaluator);
        else
            return createPrecalculatedTable();
    }
}
