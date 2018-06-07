package bishop.engine;

import bishop.base.*;
import bishop.tablebase.Classification;

import java.util.Arrays;

public class TablebasePawnEndingTable extends PawnEndingTable {

    private final PawnEndingKey key;
    private final PositionValidator positionValidator = new PositionValidator();
    private final Position position = new Position(true);
    private final TablebasePositionEvaluator tablebaseEvaluator;

    public TablebasePawnEndingTable(final PawnEndingKey key, final TablebasePositionEvaluator tablebaseEvaluator) {
        super(getInitialTable(), getInitialTable());

        this.key = key;
        this.tablebaseEvaluator = tablebaseEvaluator;
    }

    private static long[][] getInitialTable() {
        final long[][] table = new long[Color.LAST][Square.LAST];

        for (int onTurn = Color.FIRST; onTurn < Color.LAST; onTurn++)
            Arrays.fill(table[onTurn], BitBoard.FULL);

        return table;
    }

    @Override
    public int getClassification(final int kingOnTurnSquare, final int kingNotOnTurnSquare, final int onTurn) {
        final int cachedClassification = super.getClassification(kingOnTurnSquare, kingNotOnTurnSquare,onTurn);

        if (cachedClassification != Classification.UNKNOWN)
            return cachedClassification;

        final int tablebaseClassification = getClassificationFromTablebase(kingOnTurnSquare, kingNotOnTurnSquare, onTurn);

        if (tablebaseClassification == Classification.DRAW || tablebaseClassification == Classification.WIN)
            lostPositions[onTurn][kingOnTurnSquare] &= ~BitBoard.getSquareMask(kingNotOnTurnSquare);

        if (tablebaseClassification == Classification.DRAW || tablebaseClassification == Classification.LOSE)
            wonPositions[onTurn][kingNotOnTurnSquare] &= ~BitBoard.getSquareMask(kingOnTurnSquare);

        return tablebaseClassification;
    }

    public int getClassificationFromTablebase(final int kingOnTurnSquare, final int kingNotOnTurnSquare, final int onTurn) {
        final int whiteKingSquare = (onTurn == Color.WHITE) ? kingOnTurnSquare : kingNotOnTurnSquare;
        final int blackKingSquare = (onTurn == Color.WHITE) ? kingNotOnTurnSquare : kingOnTurnSquare;

        return getTablebaseEvaluation(whiteKingSquare, blackKingSquare, onTurn);
    }

    public int getTablebaseEvaluation (final int whiteKingSquare, final int blackKingSquare, final int onTurn) {
        position.clearPosition();

        final long kingOccupancy = BitBoard.getSquareMask(blackKingSquare) | BitBoard.getSquareMask(whiteKingSquare);

        position.setMoreSquaresContent(key.getWhitePawns() & ~kingOccupancy & BoardConstants.PAWN_ALLOWED_SQUARES, Piece.WHITE_PAWN);
        position.setMoreSquaresContent(key.getBlackPawns() & ~kingOccupancy & BoardConstants.PAWN_ALLOWED_SQUARES, Piece.BLACK_PAWN);

        position.setMoreSquaresContent(key.getWhitePawns() & BoardConstants.RANK_8_MASK, Piece.WHITE_QUEEN);
        position.setMoreSquaresContent(key.getBlackPawns() & BoardConstants.RANK_1_MASK, Piece.BLACK_QUEEN);

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

}
