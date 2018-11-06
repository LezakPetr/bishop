package bishopTests;

import bishop.engine.CombinedEvaluation;
import bishop.engine.Evaluation;
import org.junit.Assert;
import org.junit.Test;

public class CombinedEvaluationTest {
	@Test
	public void test() {
		for (int alpha = 0; alpha <= CombinedEvaluation.MAX_ALPHA; alpha++) {
			final long multiplier = CombinedEvaluation.getMultiplicatorForAlpha(alpha);

			for (int evaluationOpening = Evaluation.MIN; evaluationOpening <= Evaluation.MAX; evaluationOpening += 1000) {
				for (int evaluationEnding = Evaluation.MIN; evaluationEnding <= Evaluation.MAX; evaluationEnding += 1000) {
					final long combinedEvaluation = CombinedEvaluation.combine (evaluationOpening, evaluationEnding);
					final int decodedEvaluation = CombinedEvaluation.decode(
							CombinedEvaluation.ACCUMULATOR_BASE + combinedEvaluation,
							multiplier
					);

					final double t = (double) alpha / (double) CombinedEvaluation.MAX_ALPHA;

					final int expectedEvaluation = (int) Math.floor((1 - t) * evaluationOpening + t * evaluationEnding);

					Assert.assertEquals(expectedEvaluation, decodedEvaluation);
				}
			}
		}
	}
}
