package bishop.engine;

import bishop.base.BitBoard;
import bishop.tablebase.Classification;

public class PawnEndingTable implements IPawnEndingTable {
    protected final long[][] wonPositions;   // Index: onTurn, kingNotOnTurnSquare
    protected final long[][] lostPositions;   // Index: onTurn, kingOnTurnSquare

    /**
     * Constructor that creates table. Arrays are NOT copied.
     */
    public PawnEndingTable(final long[][] wonPositions, final long[][] lostPositions) {
        this.wonPositions = wonPositions;
        this.lostPositions = lostPositions;
    }

    @Override
    public int getClassification (final int kingOnTurnSquare, final int kingNotOnTurnSquare, final int onTurn) {
        final boolean won = ((wonPositions[onTurn][kingNotOnTurnSquare] & BitBoard.getSquareMask(kingOnTurnSquare)) != 0);
        final boolean lost = ((lostPositions[onTurn][kingOnTurnSquare] & BitBoard.getSquareMask(kingNotOnTurnSquare)) != 0);

        if (won && lost)
            return Classification.UNKNOWN;

        if (won)
            return Classification.WIN;

        if (lost)
            return Classification.LOSE;

        return Classification.DRAW;
    }

}
