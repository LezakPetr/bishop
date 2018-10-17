package bishop.engine;

import bishop.base.Color;
import bishop.base.IMaterialHashRead;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import utils.IntArrayBuilder;
import utils.IntUtils;

public class GameStage {
	
	public static final int FIRST = 0;

	public static final int MAX_QUEEN_COUNT = 1;
	public static final int MAX_ROOK_COUNT = 2;
	public static final int MAX_BISHOP_COUNT = 2;
	public static final int MAX_KNIGHT_COUNT = 2;

	private static final int[][][] LIGHT_FIGURE_INDICES = createLightFigureIndices();
	private static final int[][][] HEAVY_FIGURE_INDICES = createHeavyFigureIndices();

	private static final int LIGHT_FIGURE_INDEX_COUNT = 19;
	private static final int HEAVY_FIGURE_INDEX_COUNT = 8;

	private static final int HEAVY_FIGURE_INDEX_COEFF = LIGHT_FIGURE_INDEX_COUNT;
	private static final int STRENGTH_SIGNUM_FIGURE_INDEX_COEFF = HEAVY_FIGURE_INDEX_COEFF * HEAVY_FIGURE_INDEX_COUNT;
	public static final int WITH_FIGURES_COUNT = 3 * STRENGTH_SIGNUM_FIGURE_INDEX_COEFF;

	public static final int PAWNS_ONLY = 0;
	
	public static final int WITH_FIGURES_FIRST = PAWNS_ONLY + 1;
	public static final int WITH_FIGURES_LAST = WITH_FIGURES_FIRST + WITH_FIGURES_COUNT;

	public static final int LAST = WITH_FIGURES_LAST;
	public static final int COUNT = LAST - FIRST;

	private static int[][][] createLightFigureIndices() {
		final int[][][] result = new int[MAX_BISHOP_COUNT + 1][MAX_KNIGHT_COUNT + 1][2 * MAX_BISHOP_COUNT + 1];
		int index = 0;

		for (int bishopCount = 0; bishopCount <= MAX_BISHOP_COUNT; bishopCount++) {
			for (int knightCount = 0; knightCount <= MAX_KNIGHT_COUNT; knightCount++) {
				final int maxAdditionalCount = Math.min(MAX_BISHOP_COUNT - bishopCount, MAX_KNIGHT_COUNT - knightCount);

				for (int additionalBishopCount = -maxAdditionalCount; additionalBishopCount <= maxAdditionalCount; additionalBishopCount++) {
					result[bishopCount][knightCount][additionalBishopCount + MAX_BISHOP_COUNT] = index;
					index++;
				}
			}
		}

		if (index != LIGHT_FIGURE_INDEX_COUNT)
			throw new RuntimeException("Invalid light figure index count " + index);

		return result;
	}

	private static int[][][] createHeavyFigureIndices() {
		final int[][][] result = new int[MAX_QUEEN_COUNT + 1][MAX_ROOK_COUNT + 1][2 * MAX_QUEEN_COUNT + 1];
		int index = 0;

		for (int queenCount = 0; queenCount <= MAX_QUEEN_COUNT; queenCount++) {
			for (int rookCount = 0; rookCount <= MAX_ROOK_COUNT; rookCount++) {
				final int maxAdditionalCount = Math.min(MAX_QUEEN_COUNT - queenCount, (MAX_ROOK_COUNT - rookCount) / 2);

				for (int additionalQueenCount = -maxAdditionalCount; additionalQueenCount <= maxAdditionalCount; additionalQueenCount++) {
					result[queenCount][rookCount][additionalQueenCount + MAX_QUEEN_COUNT] = index;
					index++;
				}
			}
		}

		if (index != HEAVY_FIGURE_INDEX_COUNT)
			throw new RuntimeException("Invalid heavy figure index count " + index);

		return result;
	}

	public static int fromMaterial(final IMaterialHashRead materialHash) {
		int whiteKnightCount = Math.min(materialHash.getPieceCount(Color.WHITE, PieceType.KNIGHT), MAX_KNIGHT_COUNT);
		int whiteBishopCount = Math.min(materialHash.getPieceCount(Color.WHITE, PieceType.BISHOP), MAX_BISHOP_COUNT);
		int blackKnightCount = Math.min(materialHash.getPieceCount(Color.BLACK, PieceType.KNIGHT), MAX_KNIGHT_COUNT);
		int blackBishopCount = Math.min(materialHash.getPieceCount(Color.BLACK, PieceType.BISHOP), MAX_BISHOP_COUNT);
		int whiteRookCount = Math.min(materialHash.getPieceCount(Color.WHITE, PieceType.ROOK), MAX_ROOK_COUNT);
		int whiteQueenCount = Math.min(materialHash.getPieceCount(Color.WHITE, PieceType.QUEEN), MAX_QUEEN_COUNT);
		int blackRookCount = Math.min(materialHash.getPieceCount(Color.BLACK, PieceType.ROOK), MAX_ROOK_COUNT);
		int blackQueenCount = Math.min(materialHash.getPieceCount(Color.BLACK, PieceType.QUEEN), MAX_QUEEN_COUNT);

		if (whiteKnightCount == 0 && whiteBishopCount == 0 && blackKnightCount == 0 && blackBishopCount == 0 &&
		    whiteRookCount == 0 && whiteQueenCount == 0 && blackRookCount == 0 && blackQueenCount == 0)
			return PAWNS_ONLY;

		// Remove common knights
		final int commonKnightCount = Math.min (whiteKnightCount, blackKnightCount);
		whiteKnightCount -= commonKnightCount;
		blackKnightCount -= commonKnightCount;

		// Remove common bishops
		final int commonBishopCount = Math.min (whiteBishopCount, blackBishopCount);
		whiteBishopCount -= commonBishopCount;
		blackBishopCount -= commonBishopCount;

		// Knight-bishop exchange
		final int additionalWhiteBishopCount = Math.min(whiteBishopCount, blackKnightCount) - Math.min(blackBishopCount, whiteKnightCount);

		// Remove common queens
		final int commonQueenCount = Math.min (whiteQueenCount, blackQueenCount);
		whiteQueenCount -= commonQueenCount;
		blackQueenCount -= commonQueenCount;

		// Remove queen - 2 rooks exchanges
		final int whiteQueenRookExchange = Math.min(whiteQueenCount, blackRookCount / 2);
		whiteQueenCount -= whiteQueenRookExchange;
		blackRookCount -= 2 * whiteQueenRookExchange;

		final int blackQueenRookExchange = Math.min(blackQueenCount, whiteRookCount / 2);
		blackQueenCount -= blackQueenRookExchange;
		whiteRookCount -= 2 * blackQueenRookExchange;

		final int additionalWhiteQueenCount = whiteQueenRookExchange - blackQueenRookExchange;

		// Common rooks
		final int commonRookCount = Math.min (whiteRookCount, blackRookCount);
		whiteRookCount -= commonRookCount;
		blackRookCount -= commonRookCount;

		final int lightFigureIndex = LIGHT_FIGURE_INDICES[commonBishopCount][commonKnightCount][additionalWhiteBishopCount + MAX_BISHOP_COUNT];
		final int heavyFigureIndex = HEAVY_FIGURE_INDICES[commonQueenCount][commonRookCount][additionalWhiteQueenCount + MAX_QUEEN_COUNT];

		final int strength = 3 * (materialHash.getPieceCountDiff(PieceType.KNIGHT) + materialHash.getPieceCountDiff(PieceType.BISHOP)) +
				5 * materialHash.getPieceCountDiff(PieceType.ROOK) +
				10 * materialHash.getPieceCountDiff(PieceType.QUEEN);

		final int strengthSignum = 1 + Integer.signum(strength);

		final int index = WITH_FIGURES_FIRST +
				lightFigureIndex +
				heavyFigureIndex * HEAVY_FIGURE_INDEX_COEFF +
				strengthSignum * STRENGTH_SIGNUM_FIGURE_INDEX_COEFF;

		return index;
	}
}
