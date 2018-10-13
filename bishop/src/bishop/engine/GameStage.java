package bishop.engine;

import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.PieceType;
import utils.IntArrayBuilder;

public class GameStage {
	
	public static final int FIRST = 0;
	
	private static final int MAX_LIGHT_FIGURE_COUNT = 4;
	private static final int MAX_HEAVY_FIGURE_COUNT = 4;

	private static final int HEAVY_FIGURE_INDEX_COEFF = MAX_LIGHT_FIGURE_COUNT + 1;
	private static final int STRENGTH_SIGNUM_FIGURE_INDEX_COEFF = HEAVY_FIGURE_INDEX_COEFF * (MAX_HEAVY_FIGURE_COUNT + 1);
	public static final int WITH_FIGURES_COUNT = 3 * STRENGTH_SIGNUM_FIGURE_INDEX_COEFF;

	public static final int PAWNS_ONLY = 0;
	
	public static final int WITH_FIGURES_FIRST = PAWNS_ONLY + 1;
	public static final int WITH_FIGURES_LAST = WITH_FIGURES_FIRST + WITH_FIGURES_COUNT;

	public static final int LAST = WITH_FIGURES_LAST;
	public static final int COUNT = LAST - FIRST;


	public static int fromMaterial(final IMaterialHashRead materialHash) {
		final int whiteLightFigureCount = materialHash.getPieceCount(Color.WHITE, PieceType.KNIGHT) + materialHash.getPieceCount(Color.WHITE, PieceType.BISHOP);
		final int blackLightFigureCount = materialHash.getPieceCount(Color.BLACK, PieceType.KNIGHT) + materialHash.getPieceCount(Color.BLACK, PieceType.BISHOP);
		final int whiteHeavyFigureCount = materialHash.getPieceCount(Color.WHITE, PieceType.ROOK) + 2 * materialHash.getPieceCount(Color.WHITE, PieceType.QUEEN);
		final int blackHeavyFigureCount = materialHash.getPieceCount(Color.BLACK, PieceType.ROOK) + 2 * materialHash.getPieceCount(Color.BLACK, PieceType.QUEEN);

		if (whiteLightFigureCount == 0 && blackLightFigureCount == 0 && whiteHeavyFigureCount == 0 && blackHeavyFigureCount == 0)
			return PAWNS_ONLY;

		final int commonLightFigureCount = Math.min(whiteLightFigureCount, blackLightFigureCount);
		final int commonHeavyFigureCount = Math.min(whiteHeavyFigureCount, blackHeavyFigureCount);

		final int strengthDiff = 3 * (whiteLightFigureCount - blackLightFigureCount) + 5 * (whiteHeavyFigureCount - blackHeavyFigureCount);
		final int strengthSignum = Integer.signum(strengthDiff) + 1;

		final int index = WITH_FIGURES_FIRST +
				Math.min(commonLightFigureCount, MAX_LIGHT_FIGURE_COUNT) +
				Math.min(commonHeavyFigureCount, MAX_HEAVY_FIGURE_COUNT) * HEAVY_FIGURE_INDEX_COEFF +
				strengthSignum * STRENGTH_SIGNUM_FIGURE_INDEX_COEFF;

		return index;
	}
}
