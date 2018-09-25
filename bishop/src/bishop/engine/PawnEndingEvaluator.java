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
                final long matesOrStalemates;

                final long possibleKingMoves = ~key.getPawnMask(onTurn) &
                        FigureAttackTable.getItem(PieceType.KING, kingOnTurnSquare) &
                        ~BoardConstants.getPawnsAttackedSquares(oppositeColor, oppositePawns);

                final boolean isCheck = (PawnAttackTable.getItem(onTurn, kingOnTurnSquare) & oppositePawns) != 0;

                if (isCheck)
					matesOrStalemates = findAllMates(onTurn, kingOnTurnSquare, possibleKingMoves);
				else
					matesOrStalemates = findAllStalemates(onTurn, kingOnTurnSquare, pawnOccupancy, possibleKingMoves);

                final long illegalPositions = pawnOccupancy |
                        BoardConstants.getKingNearSquares(kingOnTurnSquare) |
                        BoardConstants.getPawnsAttackedSquares(onTurn, key.getPawnMask(onTurn));

                nonTerminalLegalPositions[onTurn][kingOnTurnSquare] = ~(illegalPositions | matesOrStalemates);
            }
        }
    }

	// There is no check se we are finding positions of king not on turn where
	// it is giving stalemate. The king has to block all the pawns not already blocked
	// by other pawns or king on turn. It is possible only if there is at most one
	// such pawn. If there is one such pawn we uses its target square as the square
	// where the king not on turn must be.
	// There are 2 cases - either the king on turn cannot move
	// so the king not on turn can be everywhere or it can move and the king not on turn
	// has to block it. We takes the first possible target square and calculates the positions
	// of the king not on turn where it can block it to limit the number of squares that must
	// be checked inside function getMatesOrStalemates.
	private long findAllStalemates(int onTurn, int kingOnTurnSquare, long pawnOccupancy, long possibleKingMoves) {
		final long squareBlockedByPawnsAndKingOnTurn = pawnOccupancy | BitBoard.getSquareMask(kingOnTurnSquare);
		final long pawnSquaresToBlock = BoardConstants.getPawnSingleMoveSquares(onTurn, key.getPawnMask(onTurn)) & ~squareBlockedByPawnsAndKingOnTurn;
		final int pawnSquaresToBlockCount = BitBoard.getSquareCount(pawnSquaresToBlock);

		if (pawnSquaresToBlockCount <= 1) {
			long suspiciousKingNotOnTurnSquares = (possibleKingMoves == BitBoard.EMPTY) ?
					BitBoard.FULL :
					FigureAttackTable.getItem(PieceType.KING, BitBoard.getFirstSquare(possibleKingMoves));

			if (pawnSquaresToBlockCount == 1)
				suspiciousKingNotOnTurnSquares &= pawnSquaresToBlock;

			return getMatesOrStalemates(possibleKingMoves, suspiciousKingNotOnTurnSquares);
		}
		else
			return BitBoard.EMPTY;
	}

	// There is a check so we are finding positions of king not on turn where
	// it is giving mate. There are 2 cases - either the king on turn cannot move
	// so the king not on turn can be everywhere or it can move and the king not on turn
	// has to block it. We takes the first possible target square and calculates the positions
	// of the king not on turn where it can block it to limit the number of squares that must
	// be checked inside function  getMatesOrStalemates.
	private long findAllMates(int onTurn, int kingOnTurnSquare, long possibleKingMoves) {
		final long suspiciousKingNotOnTurnSquares = (possibleKingMoves == BitBoard.EMPTY) ?
				BitBoard.FULL :
				FigureAttackTable.getItem(PieceType.KING, BitBoard.getFirstSquare(possibleKingMoves));

		final long matesOrStalemates = getMatesOrStalemates(possibleKingMoves, suspiciousKingNotOnTurnSquares);
		lostPositions[onTurn][kingOnTurnSquare] |= matesOrStalemates;

		return matesOrStalemates;
	}

	private long getMatesOrStalemates(long possibleKingMoves, long suspiciousKingNotOnTurnSquares) {
		long matesOrStalemates = BitBoard.EMPTY;

		for (BitLoop kingNotOnTurnLoop = new BitLoop(suspiciousKingNotOnTurnSquares); kingNotOnTurnLoop.hasNextSquare(); ) {
			final int kingNotOnTurnSquare = kingNotOnTurnLoop.getNextSquare();

			// All possible king moves must be blocked by king not on turn
			if ((possibleKingMoves & ~FigureAttackTable.getItem(PieceType.KING, kingNotOnTurnSquare)) == 0) {
				// Verify that there is either check or all pawns are blocked
				matesOrStalemates |= BitBoard.getSquareMask(kingNotOnTurnSquare);
			}
		}
		return matesOrStalemates;
	}

	/**
     * Calculate possible pawn captures by king. They are stored in main table as king on pawn squares.
     */
    private void processPawnCapturesByKing() {
        for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
            // King not on turn has captured pawn on turn so he must be on some square with pawn on turn not attacked by another pawn on turn
            final long possibleNotOnTurnKingSquares = key.getPawnMask(onTurn) & ~BoardConstants.getPawnsAttackedSquares(onTurn, key.getPawnMask(onTurn));

            for (BitLoop kingNotOnTurnLoop = new BitLoop(possibleNotOnTurnKingSquares); kingNotOnTurnLoop.hasNextSquare(); ) {
                final int kingNotOnTurnSquare = kingNotOnTurnLoop.getNextSquare();
                final PawnEndingKey subKey = key.removePawn (kingNotOnTurnSquare);
                final PawnEndingTable table = register.getTable(subKey);

                // King on turn cannot be attacking king not on turn or occupy it's square not he can be on any square with pawn (that would imply double capture in one move)
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

    /**
     * Processes pawn moves. They are stored in the positions before the moves
     */
    private void processPawnMoves() {
        final long pawnOccupancy = key.getPawnOccupancy();

        for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++) {
            final int notOnTurn = Color.getOppositeColor(onTurn);

            // Source squares - pawns on turn
            for (BitLoop sourceSquareLoop = new BitLoop(key.getPawnMask(onTurn)); sourceSquareLoop.hasNextSquare(); ) {
                final int sourceSquare = sourceSquareLoop.getNextSquare();

                // Target squares - possible squares for pawn moves
                final long possibleTargetSquares = PawnMoveTable.getItem(onTurn, sourceSquare) & ~pawnOccupancy;

                for (BitLoop targetSquareLoop = new BitLoop(possibleTargetSquares); targetSquareLoop.hasNextSquare(); ) {
                    final int targetSquare = targetSquareLoop.getNextSquare();
                    final long middleSquares = BetweenTable.getItem(sourceSquare, targetSquare);   // Mask of square crossed by move by two squares (if any)

                    if ((middleSquares & pawnOccupancy) == 0) {
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

                        final PawnEndingTable subTable = register.getTable(subKey);

                        // Both kings are not allowed on squares with pawns, middle square, source nor target square
                        final long disallowedKingSquares = pawnOccupancy | middleSquares | pawnChangeMask;

                        // King on turn is also not allowed on squares attacked by pawns not on turn
                        final long possibleKingOnTurnSquares = ~(BoardConstants.getPawnsAttackedSquares(notOnTurn, key.getPawnMask(notOnTurn)) | disallowedKingSquares);

                        for (final BitLoop kingOnTurnLoop = new BitLoop(possibleKingOnTurnSquares); kingOnTurnLoop.hasNextSquare(); ) {
                            final int kingOnTurnSquare = kingOnTurnLoop.getNextSquare();

                            // King not on turn is also not allowed on squares attacked by pawns on turn or by king on turn or on square occupied by king on turn
                            final long possibleKingNotOnTurnSquares = ~(BoardConstants.getPawnsAttackedSquares(onTurn, key.getPawnMask(onTurn)) | BoardConstants.getKingNearSquares(kingOnTurnSquare) | disallowedKingSquares);

                            for (final BitLoop kingNotOnTurnLoop = new BitLoop(possibleKingNotOnTurnSquares); kingNotOnTurnLoop.hasNextSquare(); ) {
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
        final int promotedPawnColor = key.getPromotedPawnColor();

        if (promotedPawnColor != Color.NONE) {
            final PawnEndingTerminalPositionEvaluator terminalPositionEvaluator = new PawnEndingTerminalPositionEvaluator(tablebaseEvaluator, register, key);
            final int onTurn = Color.getOppositeColor(promotedPawnColor);

            return terminalPositionEvaluator.calculateTable(onTurn);
        }
        else {
            final PawnEndingEvaluator evaluator = new PawnEndingEvaluator(register, key);

            return evaluator.getTable();
        }
    }

}
