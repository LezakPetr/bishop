package bishop.engine;

import bishop.base.*;
import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;

public class StaticExchangeEvaluator {

    private final Position position;
    private final PieceTypeEvaluations pieceTypeEvaluations;

    public StaticExchangeEvaluator(final Position position, final PieceTypeEvaluations pieceTypeEvaluations) {
        this.position = position;
        this.pieceTypeEvaluations = pieceTypeEvaluations;
    }

    public int getCheapestAttacker (final int color, final int square, final long effectiveOccupancy) {
        // Pawn
        final int oppositeColor = Color.getOppositeColor(color);
        final long attackingPawnMask = position.getPiecesMask(color, PieceType.PAWN) & PawnAttackTable.getItem(oppositeColor, square) & effectiveOccupancy;

        if (attackingPawnMask != 0)
            return BitBoard.getFirstSquare(attackingPawnMask);

        // Knight
        final long attackingKnightMask = position.getPiecesMask(color, PieceType.KNIGHT) & FigureAttackTable.getItem (PieceType.KNIGHT, square) & effectiveOccupancy;

        if (attackingKnightMask != 0)
            return BitBoard.getFirstSquare(attackingKnightMask);

        // Bishop
        final long attackingBishopMask = position.getPiecesMask(color, PieceType.BISHOP) & FigureAttackTable.getItem(PieceType.BISHOP, square) & effectiveOccupancy;

        for (BitLoop loop = new BitLoop(attackingBishopMask); loop.hasNextSquare(); ) {
            final int testSquare = loop.getNextSquare();

            if ((BetweenTable.getItem(square, testSquare) & effectiveOccupancy) == 0)
                return testSquare;
        }

        // Rook
        final long attackingRookMask = position.getPiecesMask(color, PieceType.ROOK) & FigureAttackTable.getItem(PieceType.ROOK, square) & effectiveOccupancy;

        for (BitLoop loop = new BitLoop(attackingRookMask); loop.hasNextSquare(); ) {
            final int testSquare = loop.getNextSquare();

            if ((BetweenTable.getItem(square, testSquare) & effectiveOccupancy) == 0)
                return testSquare;
        }

        // Queen
        final long attackingQueenMask = position.getPiecesMask(color, PieceType.QUEEN) & FigureAttackTable.getItem(PieceType.QUEEN, square) & effectiveOccupancy;

        for (BitLoop loop = new BitLoop(attackingQueenMask); loop.hasNextSquare(); ) {
            final int testSquare = loop.getNextSquare();

            if ((BetweenTable.getItem(square, testSquare) & effectiveOccupancy) == 0)
                return testSquare;
        }

        // King
        final long attackingKingMask = position.getPiecesMask(color, PieceType.KING) & FigureAttackTable.getItem (PieceType.KING, square) & effectiveOccupancy;

        if (attackingKingMask != 0)
            return BitBoard.getFirstSquare(attackingKingMask);

        return Square.NONE;
    }

    public int getStaticExchangeEvaluationOfSquare(final int color, final int square) {
        return getStaticExchangeEvaluationOfSquare(color, square, position.getOccupancy(), position.getPieceTypeOnSquare(square));
    }

    private int getStaticExchangeEvaluationOfSquare(final int color, final int square, final long effectiveOccupancy, final int pieceTypeOnSquare) {
        final int attackerSquare = getCheapestAttacker (color, square, effectiveOccupancy);

        if (attackerSquare != Square.NONE) {
            if (pieceTypeOnSquare == PieceType.NONE)
                return 0;

            if (pieceTypeOnSquare == PieceType.KING)
                return Evaluation.MAX;

            final int attackerPieceType = position.getPieceTypeOnSquare(attackerSquare);
            final int childEvaluation = getStaticExchangeEvaluationOfSquare(Color.getOppositeColor(color), square, effectiveOccupancy & ~BitBoard.of(attackerSquare), attackerPieceType);

            return Math.max(0, pieceTypeEvaluations.getPieceTypeEvaluation(pieceTypeOnSquare) - childEvaluation);
        }
        else
            return 0;
    }

    public int getStaticExchangeEvaluationOfMove(final int color, final Move move) {
        final int movingPieceType = move.getMovingPieceType();
        final int capturedPieceType = move.getCapturedPieceType();

        return pieceTypeEvaluations.getPieceTypeEvaluation(capturedPieceType) - getStaticExchangeEvaluationOfSquare(
                Color.getOppositeColor(color),
                move.getTargetSquare(),
                position.getOccupancy() & ~BitBoard.of(move.getBeginSquare()),
                movingPieceType
        );
    }

    public int getStaticExchangeEvaluationOnTurn() {
        final int notOnTurn = Color.getOppositeColor(position.getOnTurn());
        int evaluation = 0;

        for (BitLoop loop = new BitLoop(position.getColorOccupancy(notOnTurn)); loop.hasNextSquare(); ) {
            final int square = loop.getNextSquare();

            evaluation = Math.max(evaluation, getStaticExchangeEvaluationOfSquare(position.getOnTurn(), square));
        }

        return evaluation;
    }

    public boolean isMoveNonLosing(final int color, final Move move) {
        final int movingPieceType = move.getMovingPieceType();
        final int capturedPieceType = move.getCapturedPieceType();
        final int threshold = pieceTypeEvaluations.getPieceTypeEvaluation(capturedPieceType);

        return !isStaticExchangeEvaluationOfSquareAtLeast(
                Color.getOppositeColor(color),
                move.getTargetSquare(),
                threshold + 1,
                position.getOccupancy() & ~BitBoard.of(move.getBeginSquare()),
                movingPieceType
        );
    }

    public boolean isStaticExchangeEvaluationOfSquareAtLeast(final int color, final int square, final int threshold) {
        return isStaticExchangeEvaluationOfSquareAtLeast(
                color, square, threshold, position.getOccupancy(), position.getPieceTypeOnSquare(square)
        );
    }

    private boolean isStaticExchangeEvaluationOfSquareAtLeast(final int color, final int square, final int threshold, final long effectiveOccupancy, final int pieceTypeOnSquare) {
        if (pieceTypeOnSquare == PieceType.NONE)
            return threshold <= 0;

        long currentOccupancy = effectiveOccupancy;
        int currentThreshold = threshold;
        int currentColor = color;
        int currentPieceTypeOnSquare = pieceTypeOnSquare;
        boolean positive = true;

        while (true) {
            final int attackerSquare = getCheapestAttacker (currentColor, square, currentOccupancy);

            if (attackerSquare == Square.NONE)
                return (currentThreshold <= 0) == positive;

            if (currentPieceTypeOnSquare == PieceType.KING)
                return positive;

            if (currentThreshold <= 0)
                return positive;

            // Now threshold > 0

            currentOccupancy &= ~BitBoard.of(attackerSquare);
            currentThreshold = pieceTypeEvaluations.getPieceTypeEvaluation(currentPieceTypeOnSquare) - currentThreshold + 1;
            positive = !positive;
            currentColor = Color.getOppositeColor(currentColor);
            currentPieceTypeOnSquare = position.getPieceTypeOnSquare(attackerSquare);
        }
    }

}
