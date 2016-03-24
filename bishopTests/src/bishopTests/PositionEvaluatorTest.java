package bishopTests;

import org.junit.Test;

import parallel.Parallel;

import bishop.base.Position;
import bishop.engine.AttackCalculator;
import bishop.engine.EndingPositionEvaluator;
import bishop.engine.Evaluation;
import bishop.engine.IPositionEvaluator;
import bishop.engine.MiddleGamePositionEvaluator;
import bishop.engine.PositionEvaluatorSwitch;
import bishop.engine.PositionEvaluatorSwitchSettings;

public class PositionEvaluatorTest {
	
	private void testPositionEvaluatorSpeed (final Parallel parallel, final Position position, final IPositionEvaluator evaluator) {
		final int iterationCount = 2000000;
		final long t1 = System.currentTimeMillis();

		for (int i = 0; i < iterationCount; i++)
			evaluator.evaluatePosition(parallel, position, Evaluation.MIN, Evaluation.MAX, new AttackCalculator());

		final long t2 = System.currentTimeMillis();
		final double iterPerSec = (double) iterationCount * 1000 / (t2 - t1);

		System.out.println(evaluator.getClass().getSimpleName() + ": iterations per second: " + iterPerSec);
	}

	@Test
	public void speedTest() {
		final Position position = new Position();
		position.setInitialPosition();
		
		final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
		
		for (int threadCount = 1; threadCount <= Runtime.getRuntime().availableProcessors(); threadCount++) {
			System.out.println("Thread count = " + threadCount);
			
			final Parallel parallel = new Parallel(threadCount);
			parallel.startTaskRunners();
			
			testPositionEvaluatorSpeed (parallel, position, new MiddleGamePositionEvaluator(settings.getMiddleGameEvaluatorSettings()));
			testPositionEvaluatorSpeed (parallel, position, new EndingPositionEvaluator(settings.getEndingPositionEvaluatorSettings()));
			testPositionEvaluatorSpeed (parallel, position, new PositionEvaluatorSwitch(settings));
			
			parallel.stopTaskRunners();
		}
	}
}
