package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;
import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.PawnMoveTable;

public class PawnEndingEvaluator {

    private final PawnEndingKey key;

    // Legal positions: true if they are won, false if draw or lost
    // Illegal positions: false
    // Index: onTurn, kingNotOnTurnSquare
    private final long[][] wonPositions = new long[Color.LAST][Square.LAST];

    // Legal positions: true if they are lost, false if draw or won
    // Illegal positions: true
    // Index: onTurn, kingOnTurnSquare
    private final long[][] lostPositions = new long[Color.LAST][Square.LAST];

    // Legal positions: true in legal non-terminal positions where there is not possible reduction to draw or won positions
    // Index: onTurn, kingOnTurnSquare
    private final long[][] nonTerminalLegalPositions = new long[Color.LAST][Square.LAST];
    private final PawnEndingTableRegister register;

    public PawnEndingEvaluator(final PawnEndingTableRegister register, final PawnEndingKey key) {
        this.key = key;
        this.register = register;

        findMatesAndStalemates();
        processPawnCapturesByKing();
        processPawnMoves();
        findIllegalPositions();
        fillNonTerminalPositions();
    }

    /**
     * Finds mates and stalemates and initializes nonTerminalLegalPositions.
     */
    private void findMatesAndStalemates() {
        final long pawnOccupancy = key.getPawnOccupancy();

        for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
            final int oppositeColor = Color.getOppositeColor(onTurn);
            final long oppositePawns = key.getPawnMask(oppositeColor);

            for (int kingOnTurnSquare = Square.FIRST; kingOnTurnSquare < Square.LAST; kingOnTurnSquare++) {
                long matesOrStalemates = BitBoard.EMPTY;

                final long possibleKingMoves = ~key.getPawnMask(onTurn) &
                        FigureAttackTable.getItem(PieceType.KING, kingOnTurnSquare) &
                        ~BoardConstants.getPawnsAttackedSquares(oppositeColor, oppositePawns);

                final boolean isCheck = (PawnAttackTable.getItem(onTurn, kingOnTurnSquare) & oppositePawns) != 0;

                for (int kingNotOnTurnSquare = Square.FIRST; kingNotOnTurnSquare < Square.LAST; kingNotOnTurnSquare++) {
                    if ((possibleKingMoves & ~FigureAttackTable.getItem(PieceType.KING, kingNotOnTurnSquare)) == 0) {
                        final long squaresBlockedForPawns = pawnOccupancy | BitBoard.of(kingNotOnTurnSquare, kingOnTurnSquare);

                        if (isCheck || (BoardConstants.getPawnSingleMoveSquares(onTurn, key.getPawnMask(onTurn)) & ~squaresBlockedForPawns) == 0)
                            matesOrStalemates |= BitBoard.getSquareMask(kingNotOnTurnSquare);
                    }
                }

                final long illegalPositions = pawnOccupancy |
                        BoardConstants.getKingNearSquares(kingOnTurnSquare) |
                        BoardConstants.getPawnsAttackedSquares(onTurn, key.getPawnMask(onTurn));

                nonTerminalLegalPositions[onTurn][kingOnTurnSquare] = ~(illegalPositions | matesOrStalemates);

                if (isCheck)
                    lostPositions[onTurn][kingOnTurnSquare] |= matesOrStalemates;
            }
        }
    }

    private void processPawnCapturesByKing() {
        for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
            final long possibleNotOnTurnKingSquares = key.getPawnMask(onTurn) & ~BoardConstants.getPawnsAttackedSquares(onTurn, key.getPawnMask(onTurn));

            for (BitLoop kingNotOnTurnLoop = new BitLoop(possibleNotOnTurnKingSquares); kingNotOnTurnLoop.hasNextSquare(); ) {
                final int kingNotOnTurnSquare = kingNotOnTurnLoop.getNextSquare();
                final PawnEndingKey subKey = key.removePawn (kingNotOnTurnSquare);
                final PawnEndingTable table = register.getTable(subKey);
                final long possibleOnTurnKingSquares = ~BoardConstants.getKingNearSquares(kingNotOnTurnSquare) & ~key.getPawnOccupancy();

                for (BitLoop kingOnTurnLoop = new BitLoop(possibleOnTurnKingSquares); kingOnTurnLoop.hasNextSquare(); ) {
                    final int kingOnTurnSquare = kingOnTurnLoop.getNextSquare();
                    final int classification = table.getClassification(kingOnTurnSquare, kingNotOnTurnSquare, onTurn);

                    if (classification == Classification.WIN)
                        wonPositions[onTurn][kingNotOnTurnSquare] |= BitBoard.getSquareMask(kingOnTurnSquare);

                    if (classification == Classification.LOSE)
                        lostPositions[onTurn][kingOnTurnSquare] |= BitBoard.getSquareMask(kingNotOnTurnSquare);

                    nonTerminalLegalPositions[onTurn][kingOnTurnSquare] &= ~BitBoard.getSquareMask(kingNotOnTurnSquare);
                }
            }
        }
    }

    private void processPawnMoves() {
        final long pawnOccupancy = key.getPawnOccupancy();

        for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
            final int notOnTurn = Color.getOppositeColor(onTurn);

            for (BitLoop sourceSquareLoop = new BitLoop(key.getPawnMask(onTurn)); sourceSquareLoop.hasNextSquare(); ) {
                final int sourceSquare = sourceSquareLoop.getNextSquare();

                // Moves
                final long possibleTargetSquares = PawnMoveTable.getItem(onTurn, sourceSquare) & ~pawnOccupancy;

                for (BitLoop targetSquareLoop = new BitLoop(possibleTargetSquares); targetSquareLoop.hasNextSquare(); ) {
                    final int targetSquare = targetSquareLoop.getNextSquare();
                    final long middleSquares = BetweenTable.getItem(sourceSquare, targetSquare);
                    final long pawnChangeMask = BitBoard.of (sourceSquare, targetSquare);
                    final PawnEndingKey subKey;

                    if (onTurn == Color.WHITE)
                        subKey = new PawnEndingKey(
                            key.getWhitePawns() ^ pawnChangeMask,
                                key.getBlackPawns()
                        );
                    else
                        subKey = new PawnEndingKey(
                                key.getWhitePawns(),
                                key.getBlackPawns() ^ pawnChangeMask
                        );

                    if ((middleSquares & pawnOccupancy) == 0) {
                        final PawnEndingTable subTable = register.getTable(subKey);
                        final long disallowedKingSquares = pawnOccupancy | middleSquares | pawnChangeMask;
                        final long kingOnTurnMask = ~(BoardConstants.getPawnsAttackedSquares(notOnTurn, key.getPawnMask(notOnTurn)) | disallowedKingSquares);

                        for (final BitLoop kingOnTurnLoop = new BitLoop(kingOnTurnMask); kingOnTurnLoop.hasNextSquare(); ) {
                            final int kingOnTurnSquare = kingOnTurnLoop.getNextSquare();
                            final long kingNotOnTurnMask = ~(BoardConstants.getPawnsAttackedSquares(onTurn, key.getPawnMask(onTurn)) | BoardConstants.getKingNearSquares(kingOnTurnSquare) | disallowedKingSquares);

                            for (final BitLoop kingNotOnTurnLoop = new BitLoop(kingNotOnTurnMask); kingNotOnTurnLoop.hasNextSquare(); ) {
                                final int kingNotOnTurnSquare = kingNotOnTurnLoop.getNextSquare();
                                final int classification = subTable.getClassification(kingNotOnTurnSquare, kingOnTurnSquare, notOnTurn);

                                if (classification == Classification.LOSE)
                                    wonPositions[onTurn][kingNotOnTurnSquare] |= BitBoard.getSquareMask(kingOnTurnSquare);

                                if (classification == Classification.DRAW || classification == Classification.LOSE)
                                    nonTerminalLegalPositions[onTurn][kingOnTurnSquare] &= ~BitBoard.getSquareMask(kingNotOnTurnSquare);
                            }
                        }
                    }
                }
            }
        }
    }

    private void findIllegalPositions() {
        final long pawnOccupancy = key.getPawnOccupancy();

        // King on square with own pawn
        for (int kingColor = Color.FIRST; kingColor < Color.LAST; kingColor++) {
            final long pawnMask = key.getPawnMask(kingColor);

            for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++)
                setAllIllegalPositions(onTurn, kingColor, pawnMask);
        }

        // Both kings on non-empty squares
        for (BitLoop firstKingLoop = new BitLoop(pawnOccupancy); firstKingLoop.hasNextSquare(); ) {
            final int firstKingSquare = firstKingLoop.getNextSquare();

            for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++)
                wonPositions[onTurn][firstKingSquare] |= pawnOccupancy;
        }

        // King not on turn attacked by pawn
        for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
            final int notOnTurn = Color.getOppositeColor (onTurn);

            setAllIllegalPositions (onTurn, notOnTurn, BoardConstants.getPawnsAttackedSquares (onTurn, key.getPawnMask(onTurn)));
        }

        // King not on turn attacked by king
        for (int firstKingSquare = Square.FIRST; firstKingSquare < Square.LAST; firstKingSquare++) {
            final long illegalMask = BoardConstants.getKingNearSquares (firstKingSquare);

            for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++)
                wonPositions[onTurn][firstKingSquare] |= illegalMask;
        }
    }

    private void setAllIllegalPositions (final int onTurn, final int kingColor, final long kingMask) {
        if (onTurn == kingColor) {
            for (int kingNotOnTurnSquare = Square.FIRST; kingNotOnTurnSquare < Square.LAST; kingNotOnTurnSquare++)
                wonPositions[onTurn][kingNotOnTurnSquare] |= kingMask;
        }
        else {
            for (BitLoop kingLoop = new BitLoop(kingMask); kingLoop.hasNextSquare(); ) {
                final int kingSquare = kingLoop.getNextSquare();

                wonPositions[onTurn][kingSquare] |= BitBoard.FULL;
            }
        }
    }

    private void fillNonTerminalPositionsForSide(final int attackerColor) {
        final int defendantColor = Color.getOppositeColor(attackerColor);

        final long pawnOccupancy = key.getPawnOccupancy();
        final long possibleAttackerOnTurnKingSquares = ~pawnOccupancy & ~BoardConstants.getPawnsAttackedSquares(attackerColor, key.getPawnMask(attackerColor));
        final long possibleDefendantOnTurnKingSquares = ~pawnOccupancy;

        boolean isChange;

        do {
            isChange = false;

            // Attacker on turn
            for (BitLoop defendantKingLoop = new BitLoop(possibleAttackerOnTurnKingSquares); defendantKingLoop.hasNextSquare(); ) {
                final int defendantKingSquare = defendantKingLoop.getNextSquare();
                final long blockedSquares = pawnOccupancy |
                        BoardConstants.getKingNearSquares(defendantKingSquare);

                final long visitableSquares = ~blockedSquares;
                final long newMask = visitableSquares & BoardConstants.getKingsAttackedSquares(lostPositions[defendantColor][defendantKingSquare]);

                if ((newMask & ~wonPositions[attackerColor][defendantKingSquare]) != 0) {
                    wonPositions[attackerColor][defendantKingSquare] |= newMask;
                    isChange = true;
                }
            }

            // Defendant on turn
            for (BitLoop defendantKingLoop = new BitLoop(possibleDefendantOnTurnKingSquares); defendantKingLoop.hasNextSquare(); ) {
                final int defendantKingSquare = defendantKingLoop.getNextSquare();

                final long targetSquareMask = FigureAttackTable.getItem(PieceType.KING, defendantKingSquare);
                final long allowedSquares = nonTerminalLegalPositions[defendantColor][defendantKingSquare];
                long newMask = allowedSquares;

                for (BitLoop targetLoop = new BitLoop(targetSquareMask); targetLoop.hasNextSquare(); ) {
                    final int targetSquare = targetLoop.getNextSquare();

                    newMask &= wonPositions[attackerColor][targetSquare];
                }

                newMask |= lostPositions[defendantColor][defendantKingSquare] & ~allowedSquares;

                if (newMask != lostPositions[defendantColor][defendantKingSquare]) {
                    lostPositions[defendantColor][defendantKingSquare] = newMask;
                    isChange = true;
                }
            }
        } while (isChange);
    }

    private void fillNonTerminalPositions() {
        for (int attackerColor = Color.FIRST; attackerColor < Color.LAST; attackerColor++)
            fillNonTerminalPositionsForSide(attackerColor);
    }

    public PawnEndingTable getTable() {
        return new PawnEndingTable(wonPositions, lostPositions);
    }

    public static PawnEndingTable calculateTable(final TablebasePositionEvaluator tablebaseEvaluator, final PawnEndingTableRegister register, final PawnEndingKey key) {
        final boolean hasPromotedPawn = key.hasPromotedPawn();

        if (hasPromotedPawn) {
            final PawnEndingTerminalPositionEvaluator terminalPositionEvaluator = new PawnEndingTerminalPositionEvaluator(tablebaseEvaluator, register, key);

            return terminalPositionEvaluator.calculateTable();
        }
        else {
            final PawnEndingEvaluator evaluator = new PawnEndingEvaluator(register, key);

            return evaluator.getTable();
        }
    }

}
