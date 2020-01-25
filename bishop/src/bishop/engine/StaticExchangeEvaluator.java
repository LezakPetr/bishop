package bishop.engine;

import bishop.base.*;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;

/**
 * Calculator of static exchange evaluation.
 * SEE for square is calculated by considering sequence of captures of pieces on that square
 * by cheapest attackers. There is also a variant that checks if the SEE >= some threshold.
 * Note about negation:
 * To calculate SEE <= threshold we must evaluate !(SEE >= threshold + 1).
 */
public class StaticExchangeEvaluator {

    private final Position position;
    private final PieceTypeEvaluations pieceTypeEvaluations;

    private long cheapestAttackerMask;
	private int cheapestAttackerPieceType;

    public StaticExchangeEvaluator(final Position position, final PieceTypeEvaluations pieceTypeEvaluations) {
        this.position = position;
        this.pieceTypeEvaluations = pieceTypeEvaluations;
    }

    private void calculateCheapestAttacker(final int color, final int square, final long effectiveOccupancy) {
        // Pawn
        final int oppositeColor = Color.getOppositeColor(color);
        final long attackingPawnMask = position.getPiecesMask(color, PieceType.PAWN) & PawnAttackTable.getItem(oppositeColor, square) & effectiveOccupancy;

        if (attackingPawnMask != 0) {
        	cheapestAttackerMask = BitBoard.getFirstSquareMask(attackingPawnMask);
			cheapestAttackerPieceType = PieceType.PAWN;

			return;
		}

        // Knight
        final long attackingKnightMask = position.getPiecesMask(color, PieceType.KNIGHT) & FigureAttackTable.getItem (PieceType.KNIGHT, square) & effectiveOccupancy;

        if (attackingKnightMask != 0) {
			cheapestAttackerMask = BitBoard.getFirstSquareMask(attackingKnightMask);
			cheapestAttackerPieceType = PieceType.KNIGHT;

			return;
		}

        // Bishop
		final int diagonalIndex = LineIndexer.getLineIndex(CrossDirection.DIAGONAL, square, effectiveOccupancy);
		final long diagonalMask = LineAttackTable.getAttackMask(diagonalIndex);

        final long attackingBishopMask = position.getPiecesMask(color, PieceType.BISHOP) & effectiveOccupancy & diagonalMask;

        if (attackingBishopMask != 0) {
			cheapestAttackerMask = BitBoard.getFirstSquareMask(attackingBishopMask);
			cheapestAttackerPieceType = PieceType.BISHOP;

			return;
		}

        // Rook
		final int orthogonalIndex = LineIndexer.getLineIndex(CrossDirection.ORTHOGONAL, square, effectiveOccupancy);
		final long orthogonalMask = LineAttackTable.getAttackMask(orthogonalIndex);

		final long attackingRookMask = position.getPiecesMask(color, PieceType.ROOK) & effectiveOccupancy & orthogonalMask;

		if (attackingRookMask != 0) {
			cheapestAttackerMask = BitBoard.getFirstSquareMask(attackingRookMask);
			cheapestAttackerPieceType = PieceType.ROOK;

			return;
		}

        // Queen
        final long attackingQueenMask = position.getPiecesMask(color, PieceType.QUEEN) & effectiveOccupancy & (orthogonalMask | diagonalMask);

		if (attackingQueenMask != 0) {
			cheapestAttackerMask = BitBoard.getFirstSquareMask(attackingQueenMask);
			cheapestAttackerPieceType = PieceType.QUEEN;

			return;
		}

        // King
        final long attackingKingMask = position.getPiecesMask(color, PieceType.KING) & FigureAttackTable.getItem (PieceType.KING, square) & effectiveOccupancy;

        if (attackingKingMask != 0) {
			cheapestAttackerMask = BitBoard.getFirstSquareMask(attackingKingMask);
			cheapestAttackerPieceType = PieceType.KING;

			return;
		}

		cheapestAttackerMask = BitBoard.EMPTY;
		cheapestAttackerPieceType = PieceType.NONE;
    }

    public int getStaticExchangeEvaluationOfSquare(final int color, final int square) {
        return getStaticExchangeEvaluationOfSquare(color, square, position.getOccupancy(), position.getPieceTypeOnSquare(square));
    }

    private int getStaticExchangeEvaluationOfSquare(final int color, final int square, final long effectiveOccupancy, final int pieceTypeOnSquare) {
        calculateCheapestAttacker(color, square, effectiveOccupancy);

        if (cheapestAttackerMask != BitBoard.EMPTY) {
            if (pieceTypeOnSquare == PieceType.NONE)
                return 0;

            if (pieceTypeOnSquare == PieceType.KING)
                return Evaluation.MAX;

            final int childEvaluation = getStaticExchangeEvaluationOfSquare(Color.getOppositeColor(color), square, effectiveOccupancy & ~cheapestAttackerMask, cheapestAttackerPieceType);

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
            calculateCheapestAttacker(currentColor, square, currentOccupancy);

            if (cheapestAttackerMask == BitBoard.EMPTY)
                return (currentThreshold <= 0) == positive;

            if (currentPieceTypeOnSquare == PieceType.KING)
                return positive;

            if (currentThreshold <= 0)
                return positive;

            // Now threshold > 0

            currentOccupancy &= ~cheapestAttackerMask;
            currentThreshold = pieceTypeEvaluations.getPieceTypeEvaluation(currentPieceTypeOnSquare) - currentThreshold + 1;
            positive = !positive;
            currentColor = Color.getOppositeColor(currentColor);
            currentPieceTypeOnSquare = cheapestAttackerPieceType;
        }
    }

}
