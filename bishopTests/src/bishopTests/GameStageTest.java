package bishopTests;

import bishop.base.*;
import bishop.engine.GameStage;
import org.junit.Assert;
import org.junit.Test;
import utils.IntUtils;

import java.util.HashMap;
import java.util.Map;

public class GameStageTest {
	@Test
	public void testNoCollision() {
		final Map<Integer, MaterialHash> generatedStages = new HashMap<>();

		for (int lightFigureCount = 0; lightFigureCount <= GameStage.MAX_BISHOP_COUNT + GameStage.MAX_KNIGHT_COUNT; lightFigureCount++) {
			final int minBishopCount = Math.max (lightFigureCount - GameStage.MAX_KNIGHT_COUNT, 0);
			final int maxBishopCount = Math.min(lightFigureCount, GameStage.MAX_BISHOP_COUNT);

			for (int whiteBishopCount = minBishopCount; whiteBishopCount <= maxBishopCount; whiteBishopCount++) {
				for (int blackBishopCount = minBishopCount; blackBishopCount <= maxBishopCount; blackBishopCount++) {
					for (int heavyFigureCount = 0; heavyFigureCount <= 2 * GameStage.MAX_QUEEN_COUNT + GameStage.MAX_ROOK_COUNT; heavyFigureCount++) {
						final int minQueenCount = Math.max (IntUtils.divideRoundUp(heavyFigureCount - GameStage.MAX_ROOK_COUNT, 2), 0);
						final int maxQueenCount = Math.min(heavyFigureCount / 2, GameStage.MAX_QUEEN_COUNT);

						for (int whiteQueenCount = minQueenCount; whiteQueenCount <= maxQueenCount; whiteQueenCount++) {
							for (int blackQueenCount = minQueenCount; blackQueenCount <= maxQueenCount; blackQueenCount++) {
								for (int addedCount = -1; addedCount <= +1; addedCount++) {
									final int whiteAddedCount = Math.max(addedCount, 0);
									final int blackAddedCount = Math.max(-addedCount, 0);
									final MaterialHash hash = new MaterialHash();
									hash.addPiece(Color.WHITE, PieceType.BISHOP, whiteBishopCount);
									hash.addPiece(Color.WHITE, PieceType.KNIGHT, lightFigureCount - whiteBishopCount);
									hash.addPiece(Color.WHITE, PieceType.QUEEN, whiteQueenCount);
									hash.addPiece(Color.WHITE, PieceType.ROOK, heavyFigureCount - 2 * whiteQueenCount + whiteAddedCount);
									hash.addPiece(Color.BLACK, PieceType.BISHOP, blackBishopCount);
									hash.addPiece(Color.BLACK, PieceType.KNIGHT, lightFigureCount - blackBishopCount);
									hash.addPiece(Color.BLACK, PieceType.QUEEN, blackQueenCount);
									hash.addPiece(Color.BLACK, PieceType.ROOK, heavyFigureCount - 2 * blackQueenCount + blackAddedCount);

									final int stage = GameStage.fromMaterial(hash);

									if (generatedStages.containsKey(stage))
										throw new RuntimeException("Dupplicated stage: " + stage + ", " + generatedStages.get(stage) + ", " + hash);

									generatedStages.put(stage, hash);
								}
							}
						}
					}
				}
			}
		}

		Assert.assertEquals(456, generatedStages.size());
	}
}
