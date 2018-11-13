package bishopTests;

import bishop.engine.CombinedEvaluation;
import bishop.engine.Evaluation;
import bishop.engine.GameStage;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;

public class CombinedEvaluationTest {
	@Test
	public void test() {
		for (int gameStage = GameStage.FIRST; gameStage < GameStage.LAST; gameStage++) {
			final long multiplier = CombinedEvaluation.getMultiplicatorForGameStage(gameStage);

			for (int evaluationOpening = Evaluation.MIN; evaluationOpening <= Evaluation.MAX; evaluationOpening += 10000) {
				for (int evaluationMiddleGame = Evaluation.MIN; evaluationMiddleGame <= Evaluation.MAX; evaluationMiddleGame += 10000) {
					for (int evaluationEnding = Evaluation.MIN; evaluationEnding <= Evaluation.MAX; evaluationEnding += 10000) {
						final long combinedEvaluation = CombinedEvaluation.combine(evaluationOpening, evaluationMiddleGame, evaluationEnding);
						final int decodedEvaluation = CombinedEvaluation.decode(
								CombinedEvaluation.ACCUMULATOR_BASE + combinedEvaluation,
								multiplier
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
}
