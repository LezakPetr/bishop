package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;
import bishop.tablebase.TableResult;
import bishop.tables.FigureAttackTable;

import java.util.Arrays;

public class BlockedPawnEndingEvaluator {

    private PawnEndingKey key;
    private final long[][] wonPositions = new long[Color.LAST][Square.LAST];   // Index: onTurn, kingNotOnTurnSquare
    private final long[][] lostPositions = new long[Color.LAST][Square.LAST];   // Index: onTurn, kingOnTurnSquare
    private final long[][] nonTerminalLegalPositions = new long[Color.LAST][Square.LAST];   // Index: onTurn, kingOnTurnSquare
    private final TablebasePositionEvaluator tablebaseEvaluator;
    private final Position position = new Position(true);
    private final PositionValidator positionValidator = new PositionValidator();

    public BlockedPawnEndingEvaluator (final TablebasePositionEvaluator tablebaseEvaluator, final PawnEndingKey key) {
        this.tablebaseEvaluator = tablebaseEvaluator;
        this.key = key;

        findMatesAndStalemates();
        findTerminalPositions();
        findIllegalPositions();
        fillNonTerminalPositions();
    }

    private boolean isPromotedPawn() {
        return (key.getPawnOccupancy() & BoardConstants.RANK_18_MASK) != 0;
    }

    private void findMatesAndStalemates() {
        final long pawnOccupancy = key.getPawnOccupancy();

        for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
            final int oppositeColor = Color.getOppositeColor(onTurn);

            for (int kingOnTurnSquare = Square.FIRST; kingOnTurnSquare < Square.LAST; kingOnTurnSquare++) {
                long blockedSquares = pawnOccupancy |
                        FigureAttackTable.getItem(PieceType.KING, kingOnTurnSquare) |
                        BitBoard.getSquareMask(kingOnTurnSquare) |
                        BoardConstants.getPawnsAttackedSquares(onTurn, key.getPawnMask(onTurn));

                final long possibleKingMoves = ~key.getPawnMask(onTurn) &
                        FigureAttackTable.getItem(PieceType.KING, kingOnTurnSquare) &
                        ~BoardConstants.getPawnsAttackedSquares(oppositeColor, key.getPawnMask(oppositeColor));

                for (int kingNotOnTurnSquare = Square.FIRST; kingNotOnTurnSquare < Square.LAST; kingNotOnTurnSquare++) {
                    if ((possibleKingMoves & ~FigureAttackTable.getItem(PieceType.KING, kingNotOnTurnSquare)) == 0) {
                        blockedSquares |= BitBoard.getSquareMask(kingNotOnTurnSquare);
                    }
                }

                nonTerminalLegalPositions[onTurn][kingOnTurnSquare] = ~blockedSquares;
            }
        }
    }

    private void findTerminalPositions() {
        for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
            for (BitLoop kingNotOnTurnLoop = new BitLoop(key.getPawnMask(onTurn)); kingNotOnTurnLoop.hasNextSquare(); ) {
                final int kingNotOnTurnSquare = kingNotOnTurnLoop.getNextSquare();

                for (int kingOnTurnSquare = Square.FIRST; kingOnTurnSquare < Square.LAST; kingOnTurnSquare++) {
                    final int classification = (onTurn == Color.WHITE) ?
                            getTerminalClassification(kingOnTurnSquare, kingNotOnTurnSquare, onTurn) :
                            getTerminalClassification(kingNotOnTurnSquare, kingOnTurnSquare, onTurn);

                    if (classification == Classification.WIN)
                        wonPositions[onTurn][kingNotOnTurnSquare] |= BitBoard.getSquareMask(kingOnTurnSquare);

                    if (classification == Classification.LOSE)
                        lostPositions[onTurn][kingOnTurnSquare] |= BitBoard.getSquareMask(kingNotOnTurnSquare);
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
            final long illegalMask = BitBoard.getSquareMask (firstKingSquare) | FigureAttackTable.getItem (PieceType.KING, firstKingSquare);

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

    public int getTerminalClassification (final int whiteKingSquare, final int blackKingSquare, final int onTurn) {
        position.clearPosition();

        final long kingOccupancy = BitBoard.getSquareMask(blackKingSquare) | BitBoard.getSquareMask(whiteKingSquare);

        position.setMoreSquaresContent(key.getWhitePawns() & ~kingOccupancy, Piece.WHITE_PAWN);
        position.setMoreSquaresContent(key.getBlackPawns() & ~kingOccupancy, Piece.BLACK_PAWN);

        position.setSquareContent(whiteKingSquare, Piece.WHITE_KING);
        position.setSquareContent(blackKingSquare, Piece.BLACK_KING);

        position.setOnTurn(onTurn);
        position.refreshCachedData();

        positionValidator.setPosition(position);

        if (positionValidator.checkPosition())
            return Evaluation.getClassification (tablebaseEvaluator.evaluatePosition(position, 0));
        else
            return Classification.ILLEGAL;
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
                        FigureAttackTable.getItem(PieceType.KING, defendantKingSquare) |
                        BitBoard.getSquareMask(defendantKingSquare);

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

    public int getClassification (final int whiteKingSquare, final int blackKingSquare, final int onTurn) {
        final int kingOnTurnSquare;
        final int kingNotOnTurnSquare;

        if (onTurn == Color.WHITE) {
            kingOnTurnSquare = whiteKingSquare;
            kingNotOnTurnSquare = blackKingSquare;
        }
        else {
            kingOnTurnSquare = blackKingSquare;
            kingNotOnTurnSquare = whiteKingSquare;
        }

        if ((wonPositions[onTurn][kingNotOnTurnSquare] & BitBoard.getSquareMask(kingOnTurnSquare)) != 0)
            return Classification.WIN;

        if ((lostPositions[onTurn][kingOnTurnSquare] & BitBoard.getSquareMask(kingNotOnTurnSquare)) != 0)
            return Classification.LOSE;

        return Classification.DRAW;
    }

    private void print(final long[] data) {
        for (int square = Square.FIRST; square < Square.LAST; square++) {
            System.out.print (Square.toString(square) + ": ");
            System.out.println(BitBoard.toString(data[square]));
        }
    }
}
