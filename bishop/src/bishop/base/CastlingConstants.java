package bishop.base;

public final class CastlingConstants {

	private final long middleSquareMask;   // Mask of squares between king and rook before castling
	private final int rookBeginSquare;   // Begin square of rook in castling
	private final int rookTargetSquare;   // Target square of rook in castling
	private final int kingBeginSquare;   // Begin square of king in the castling
	private final int kingTargetSquare;   // Target squares of king in castling
	private final long kingChangeMask;   // Contains changes of king mask for given type of castling
	private final long rookChangeMask;   // Contains changes of rook mask for given type of castling


	private static final CastlingConstants[] INSTANCES = {
			new CastlingConstants(
					BitBoard.of(Square.F1, Square.G1),
					Square.H1,
					Square.F1,
					Square.E1,
					Square.G1
			),
			new CastlingConstants(
					BitBoard.of(Square.B1, Square.C1, Square.D1),
					Square.A1,
					Square.D1,
					Square.E1,
					Square.C1
			),
			new CastlingConstants(
					BitBoard.of(Square.F8, Square.G8),
					Square.H8,
					Square.F8,
					Square.E8,
					Square.G8
			),
			new CastlingConstants(
					BitBoard.of(Square.B8, Square.C8, Square.D8),
					Square.A8,
					Square.D8,
					Square.E8,
					Square.C8
			)
	};

	private CastlingConstants (final long middleSquareMask, final int rookBeginSquare, final int rookTargetSquare, final int kingBeginSquare, final int kingTargetSquare) {
		this.middleSquareMask = middleSquareMask;
		this.rookBeginSquare = rookBeginSquare;
		this.rookTargetSquare = rookTargetSquare;
		this.kingBeginSquare = kingBeginSquare;
		this.kingTargetSquare = kingTargetSquare;
		this.kingChangeMask = BitBoard.of(kingBeginSquare, kingTargetSquare);
		this.rookChangeMask = BitBoard.of(rookBeginSquare, rookTargetSquare);
	}

	public long getMiddleSquareMask() {
		return middleSquareMask;
	}

	public int getRookBeginSquare() {
		return rookBeginSquare;
	}

	public int getKingMiddleSquare() {
		return rookTargetSquare;
	}

	public int getRookTargetSquare() {
		return rookTargetSquare;
	}

	public int getKingBeginSquare() {
		return kingBeginSquare;
	}

	public int getKingTargetSquare() {
		return kingTargetSquare;
	}

	public long getKingChangeMask() {
		return kingChangeMask;
	}

	public long getRookChangeMask() {
		return rookChangeMask;
	}

	public static CastlingConstants of (final int color, final int castlingType) {
		final int index = (color << CastlingType.BIT_COUNT) + castlingType;

		return INSTANCES[index];
	}

}
