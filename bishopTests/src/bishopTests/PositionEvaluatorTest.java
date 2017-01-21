package bishopTests;

import java.util.function.Supplier;

import org.junit.Test;

import bishop.base.DefaultAdditiveMaterialEvaluator;
import bishop.base.IMaterialEvaluator;
import bishop.base.Position;
import bishop.engine.AlgebraicPositionEvaluation;
import bishop.engine.AttackCalculator;
import bishop.engine.EndingPositionEvaluator;
import bishop.engine.Evaluation;
import bishop.engine.IPositionEvaluation;
import bishop.engine.IPositionEvaluator;
import bishop.engine.MiddleGamePositionEvaluator;
import bishop.engine.PawnStructureCache;
import bishop.engine.PositionEvaluatorSwitch;
import bishop.engine.PositionEvaluatorSwitchSettings;

public class PositionEvaluatorTest {
	
	private void testPositionEvaluatorSpeed (final Position position, final IPositionEvaluator evaluator) {
		final int iterationCount = 2000000;
		final long t1 = System.currentTimeMillis();
		final Supplier<IPositionEvaluation> evaluationFactory = AlgebraicPositionEvaluation.getTestingFactory();

		for (int i = 0; i < iterationCount; i++)
			evaluator.evaluatePosition(position, Evaluation.MIN, Evaluation.MAX, new AttackCalculator(evaluationFactory));

		final long t2 = System.currentTimeMillis();
		final double iterPerSec = (double) iterationCount * 1000 / (t2 - t1);

		System.out.println(evaluator.getClass().getSimpleName() + ": iterations per second: " + iterPerSec);
	}

	@Test
	public void speedTest() {
		final Position position = new Position();
		position.setInitialPosition();
		
		final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
		final IMaterialEvaluator materialEvaluator = DefaultAdditiveMaterialEvaluator.getInstance();
		
		final Supplier<IPositionEvaluation> evaluationFactory = AlgebraicPositionEvaluation.getTestingFactory();
		final PawnStructureCache pawnStructureCache = new PawnStructureCache();
		
		testPositionEvaluatorSpeed (position, new MiddleGamePositionEvaluator(settings.getMiddleGameEvaluatorSettings(), materialEvaluator, pawnStructureCache, evaluationFactory));
		testPositionEvaluatorSpeed (position, new EndingPositionEvaluator(materialEvaluator, pawnStructureCache, evaluationFactory));
		testPositionEvaluatorSpeed (position, new PositionEvaluatorSwitch(settings, materialEvaluator, evaluationFactory));
	}
}
