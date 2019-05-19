package bishopTests;

import bishop.engine.CombinedEvaluation;
import bishop.engine.CombinedEvaluationDecoder;
import bishop.engine.Evaluation;
import bishop.engine.GameStage;
import org.junit.Assert;
import org.junit.Test;

public class CombinedEvaluationTest {
	@Test
	public void testDecoder() {
		for (int gameStage = GameStage.FIRST; gameStage < GameStage.LAST; gameStage++) {
			final CombinedEvaluationDecoder decoder = CombinedEvaluation.getDecoderForGameStage(gameStage);

			for (int evaluationOpening = Evaluation.MIN; evaluationOpening <= Evaluation.MAX; evaluationOpening += 10000) {
				for (int evaluationMiddleGame = Evaluation.MIN; evaluationMiddleGame <= Evaluation.MAX; evaluationMiddleGame += 10000) {
					for (int evaluationEnding = Evaluation.MIN; evaluationEnding <= Evaluation.MAX; evaluationEnding += 10000) {
						final long combinedEvaluation = CombinedEvaluation.combine(evaluationOpening, evaluationMiddleGame, evaluationEnding);
						final int decodedEvaluation = decoder.decode(
								CombinedEvaluation.ACCUMULATOR_BASE + combinedEvaluation
						);

						final int expectedEvaluation = (
								evaluationOpening * CombinedEvaluation.getComponentMultiplicator(gameStage, CombinedEvaluation.COMPONENT_OPENING) +
								evaluationMiddleGame * CombinedEvaluation.getComponentMultiplicator(gameStage, CombinedEvaluation.COMPONENT_MIDDLE_GAME) +
								evaluationEnding * CombinedEvaluation.getComponentMultiplicator(gameStage, CombinedEvaluation.COMPONENT_ENDING)
						) / CombinedEvaluation.MAX_ALPHA;

						Assert.assertEquals(expectedEvaluation, decodedEvaluation);
					}
				}
			}
		}
	}

	private void testComponentMultiplicatorsForGameStage(final int gameStage, final int multiplicatorOpening, final int multiplicatorMiddleGame, final int multiplicatorEnding) {
		Assert.assertEquals(multiplicatorOpening, CombinedEvaluation.getComponentMultiplicator(gameStage, CombinedEvaluation.COMPONENT_OPENING));
		Assert.assertEquals(multiplicatorMiddleGame, CombinedEvaluation.getComponentMultiplicator(gameStage, CombinedEvaluation.COMPONENT_MIDDLE_GAME));
		Assert.assertEquals(multiplicatorEnding, CombinedEvaluation.getComponentMultiplicator(gameStage, CombinedEvaluation.COMPONENT_ENDING));
	}

	@Test
	public void testComponentMultiplicators() {
		testComponentMultiplicatorsForGameStage(GameStage.FIRST, 0, 0, CombinedEvaluation.MAX_ALPHA);
		testComponentMultiplicatorsForGameStage(CombinedEvaluation.MIDDLE_GAME_TRESHOLD, 0, CombinedEvaluation.MAX_ALPHA, 0);
		testComponentMultiplicatorsForGameStage(GameStage.LAST - 1, CombinedEvaluation.MAX_ALPHA, 0, 0);
	}
}
