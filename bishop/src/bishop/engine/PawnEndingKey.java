package bishop.engine;

import bishop.base.*;
import bishop.tables.PawnEndingFileTable;
import utils.Mixer;

public class PawnEndingKey {

    public static final PawnEndingKey EMPTY = new PawnEndingKey(BitBoard.EMPTY, BitBoard.EMPTY);

    private final long whitePawns;
    private final long blackPawns;

    private final int hash;

    public PawnEndingKey (final long whitePawns, final long blackPawns) {
        this.whitePawns = whitePawns;
        this.blackPawns = blackPawns;

        this.hash = Mixer.mixLongToInt(31 * whitePawns + blackPawns);
    }

    public PawnEndingKey (final long... pawnMasks) {
        this(pawnMasks[Color.WHITE], pawnMasks[Color.BLACK]);
    }

    public long getWhitePawns() {
        return whitePawns;
    }

    public long getBlackPawns() {
        return blackPawns;
    }

    public long getPawnOccupancy() {
        return whitePawns | blackPawns;
    }

    public long getPawnMask(final int color) {
        if (color == Color.WHITE)
            return whitePawns;
        else
            return blackPawns;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj == null || this.getClass() != obj.getClass())
            return false;

        final PawnEndingKey that = (PawnEndingKey) obj;

        return this.hash == that.hash &&
                this.whitePawns == that.whitePawns &&
                this.blackPawns == that.blackPawns;
    }

    @Override
    public String toString() {
        return "white pawns = " + BitBoard.toString(whitePawns) + ", black pawns = " + BitBoard.toString(blackPawns);
    }

	/**
	 * Returns color of promoted pawn(s), if any. If pawn of both colors has promoted, the result is undefined.
	 * @return color of promoted pawn or Color.NONE
	 */
	public int getPromotedPawnColor() {
        final long occupancy = getPawnOccupancy();

        for (int color = Color.FIRST; color < Color.LAST; color++) {
            if ((occupancy & BoardConstants.getRankMask(BoardConstants.getPawnPromotionRank(color))) != 0)
                return color;
        }

        return Color.NONE;
    }

    public PawnEndingKey removePawn(final int square) {
        final long mask = ~BitBoard.getSquareMask(square);

        return new PawnEndingKey(
                whitePawns & mask,
                blackPawns & mask
        );
    }

    public PawnEndingKey addPawn(final int color, final int square) {
        final long mask = BitBoard.getSquareMask(square);

        if (color == Color.WHITE)
            return new PawnEndingKey(whitePawns | mask, blackPawns);
        else
            return new PawnEndingKey(whitePawns, blackPawns | mask);
    }

    public MaterialHash getMaterialHash() {
        final MaterialHash materialHash = new MaterialHash();

        for (int color = Color.FIRST; color < Color.LAST; color++) {
            final long pawnMask = getPawnMask(color);

            final int pawnCount = BitBoard.getSquareCount(pawnMask & BoardConstants.PAWN_ALLOWED_SQUARES);
            materialHash.addPiece(color, PieceType.PAWN, pawnCount);

            final int queenCount = BitBoard.getSquareCountSparse(pawnMask & ~BoardConstants.PAWN_ALLOWED_SQUARES);
            materialHash.addPiece(color, PieceType.QUEEN, queenCount);
        }

        return materialHash;
    }

    public long estimateComplexity() {
        if (isPawnPawnCapturePossible())
            return Long.MAX_VALUE;   // Not possible to calculate

        long tableCount = 1;

        for (int file = File.FIRST; file < File.LAST; file++) {
            final int fileComplexity = PawnEndingFileTable.getComplexity(this, file);

            tableCount *= fileComplexity;
        }

        return tableCount;
    }

    private boolean isPawnPawnCapturePossible() {
    	for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
    		final long ownPawnMask = getPawnMask(color);
			final long oppositePawnMask = getPawnMask(oppositeColor);

			for (BitLoop ownLoop = new BitLoop(ownPawnMask); ownLoop.hasNextSquare(); ) {
				final int ownSquare = ownLoop.getNextSquare();
				final long capturablePawnMask = BoardConstants.getFrontSquaresOnNeighborFiles(color, ownSquare) & oppositePawnMask;
				final int ownRank = Square.getRank(ownSquare);
				final int ownFile = Square.getFile(ownSquare);

				for (BitLoop oppositeLoop = new BitLoop(capturablePawnMask); oppositeLoop.hasNextSquare(); ) {
					final int oppositeSquare = oppositeLoop.getNextSquare();
					final int oppositeRank = Square.getRank(oppositeSquare);

					if (Math.abs(oppositeRank - ownRank) != 2)
						return true;

					final int oppositeFile = Square.getFile(oppositeSquare);
					final int middleRank = (ownRank + oppositeRank) / 2;

					if (!BitBoard.containsSquare(oppositePawnMask, Square.onFileRank(ownFile, middleRank)) ||
						!BitBoard.containsSquare(ownPawnMask, Square.onFileRank(oppositeFile, middleRank)))
						return true;
				}
			}
		}

		return false;
    }

}
