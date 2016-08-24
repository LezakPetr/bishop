package bishopTests;

import org.junit.Test;

import bishop.base.Position;
import bishop.engine.AttackCalculator;
import bishop.engine.EndingPositionEvaluator;
import bishop.engine.Evaluation;
import bishop.engine.IPositionEvaluator;
import bishop.engine.MiddleGamePositionEvaluator;
import bishop.engine.PositionEvaluatorSwitch;
import bishop.engine.PositionEvaluatorSwitchSettings;

public class PositionEvaluatorTest {
	
	private void testPositionEvaluatorSpeed (final Position position, final IPositionEvaluator evaluator) {
		final int iterationCount = 2000000;
		final long t1 = System.currentTimeMillis();

		for (int i = 0; i < iterationCount; i++)
			evaluator.evaluatePosition(position, Evaluation.MIN, Evaluation.MAX, new AttackCalculator());

		final long t2 = System.currentTimeMillis();
		final double iterPerSec = (double) iterationCount * 1000 / (t2 - t1);

		System.out.println(evaluator.getClass().getSimpleName() + ": iterations per second: " + iterPerSec);
	}

	@Test
	public void speedTest() {
		final Position position = new Position();
		position.setInitialPosition();
		
		final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
		testPositionEvaluatorSpeed (position, new MiddleGamePositionEvaluator(settings.getMiddleGameEvaluatorSettings()));
		testPositionEvaluatorSpeed (position, new EndingPositionEvaluator(settings.getEndingPositionEvaluatorSettings()));
		testPositionEvaluatorSpeed (position, new PositionEvaluatorSwitch(settings));
	}
}
