package bishop.engine;

import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.PieceType;
import utils.IntArrayBuilder;

public class GameStage {

	public static final int FIRST = 0;
	public static final int LAST = 25;

	public static final int COUNT = LAST - FIRST;

	public static final int PAWNS_ONLY = 0;

	public static final int WITH_FIGURES_FIRST = PAWNS_ONLY + 1;
	public static final int WITH_FIGURES_LAST = LAST;
	public static final int WITH_FIGURES_COUNT = WITH_FIGURES_LAST - WITH_FIGURES_FIRST;

	private static final int[] PIECE_TYPE_SHIFTS = new IntArrayBuilder(LAST)
			.put(PieceType.KNIGHT, 0)
			.put(PieceType.BISHOP, 0)
			.put(PieceType.ROOK, 1)
			.put(PieceType.QUEEN, 2)
			.build();

	public static int fromMaterial(final IMaterialHashRead materialHash) {
		int stage = 0;

		for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
			final int shift = PIECE_TYPE_SHIFTS[pieceType];

			for (int color = Color.FIRST; color < Color.LAST; color++)
				stage += materialHash.getPieceCount(color, pieceType) << shift;
		}

		return Math.min(stage, LAST - 1);
	}
}