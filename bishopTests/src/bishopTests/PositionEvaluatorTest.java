package bishopTests;

import java.util.function.Supplier;

import org.junit.Test;

import bishop.base.Position;
import bishop.engine.AlgebraicPositionEvaluation;
import bishop.engine.AttackCalculator;
import bishop.engine.GeneralPositionEvaluator;
import bishop.engine.IPositionEvaluation;
import bishop.engine.IPositionEvaluator;
import bishop.engine.PawnStructureCache;
import bishop.engine.PositionEvaluatorSwitch;
import bishop.engine.PositionEvaluatorSwitchSettings;

public class PositionEvaluatorTest {
	
	private void testPositionEvaluatorSpeed (final Position position, final IPositionEvaluator evaluator, final boolean withPositionalEvaluation) {
		final int iterationCount = 4000000;
		final AttackCalculator attackCalculator = new AttackCalculator();

		for (int i = 0; i < 2; i++) {
			final long t1 = System.currentTimeMillis();

			for (int j = 0; j < iterationCount; j++) {
				evaluator.evaluateTactical(position, attackCalculator);

				if (withPositionalEvaluation)
					evaluator.evaluatePositional(attackCalculator);
			}

			if (i == 1) {
				final long t2 = System.currentTimeMillis();
				final double iterPerSec = (double) iterationCount * 1000 / (t2 - t1);

				System.out.println(evaluator.getClass().getSimpleName() + " with positional = " + withPositionalEvaluation + ": iterations per second: " + iterPerSec);
			}
		}
	}
	
	private void testPositionEvaluatorSpeed (final Position position, final IPositionEvaluator evaluator) {
		final boolean[] withPositionalValues = { false, true };
		
		for (boolean withPositionalEvaluation: withPositionalValues) {
			testPositionEvaluatorSpeed(position, evaluator, withPositionalEvaluation);
		}
	}

	@Test
	public void speedTest() {
		final Position position = new Position();
		position.setInitialPosition();
		
		final PositionEvaluatorSwitchSettings settings = new PositionEvaluatorSwitchSettings();
		
		final Supplier<IPositionEvaluation> evaluationFactory = AlgebraicPositionEvaluation.getTestingFactory();
		
		testPositionEvaluatorSpeed (position, new GeneralPositionEvaluator(settings.getGeneralEvaluatorSettings(), evaluationFactory));
		testPositionEvaluatorSpeed (position, new PositionEvaluatorSwitch(settings, evaluationFactory));
	}
}
