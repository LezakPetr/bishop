package bishopTests;

import java.io.IOException;
import java.util.function.Supplier;

import bishop.base.*;
import bishop.engine.*;
import org.junit.Assert;
import org.junit.Test;

public class PositionEvaluatorTest {

	private static final String[] TESTED_POSITIONS = {
		"3k4/8/3K4/8/8/8/8/8 w - - 0 1",
		"3k1R2/8/3K4/8/8/8/8/8 b - - 0 1",
		"8/Q7/8/8/8/5kn1/5p2/5K2 w - - 0 1",
		"k7/1R6/2K5/8/8/8/8/8 b - - 0 1",
		"2k5/8/2K1R3/8/8/8/8/8 w - - 0 1",
		"7k/8/8/6RK/8/8/8/8 w - - 0 1",
		"8/8/8/8/6rk/8/8/7K b - - 0 1",
		"QR6/7k/8/8/7q/8/6P1/6K1 b - - 0 1"
	};
	
	private void testPositionEvaluatorSpeed (final Position position, final IPositionEvaluator evaluator, final boolean withPositionalEvaluation) {
		final int iterationCount = 4000000;
		final MobilityCalculator mobilityCalculator = new MobilityCalculator();
		mobilityCalculator.calculate(position);

		for (int i = 0; i < 2; i++) {
			final long t1 = System.currentTimeMillis();

			for (int j = 0; j < iterationCount; j++) {
				evaluator.evaluateTactical(position, mobilityCalculator);

				if (withPositionalEvaluation)
					evaluator.evaluatePositional();
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
		
		final Supplier<IPositionEvaluation> evaluationFactory = AlgebraicPositionEvaluation.getAlgebraicTestingFactory();
		
		testPositionEvaluatorSpeed (position, new GeneralPositionEvaluator(evaluationFactory));
		testPositionEvaluatorSpeed (position, new PositionEvaluatorSwitch(evaluationFactory));
	}

	private int calculateEvaluation (final Position position, final IPositionEvaluator evaluator, final MobilityCalculator mobilityCalculator) {
		int evaluation = evaluator.evaluateTactical(position, mobilityCalculator).getEvaluation();
		evaluation += evaluator.evaluatePositional().getEvaluation();

		return evaluation;
	}

	private int testPositionConsistency (final Position position) {
		position.setCombinedPositionEvaluationTable(new CombinedPositionEvaluationTable(PositionEvaluationCoeffs.random()));
		position.refreshCachedData();

		final MobilityCalculator mobilityCalculator = new MobilityCalculator();
		mobilityCalculator.calculate(position);

		final Supplier<IPositionEvaluation> algebraicEvaluationFactory = AlgebraicPositionEvaluation.getAlgebraicTestingFactory();
		final IPositionEvaluator algebraicEvaluator = new GeneralPositionEvaluator(algebraicEvaluationFactory);

		final int firstAlgebraicEvaluation = calculateEvaluation(position, algebraicEvaluator, mobilityCalculator);
		final int secondAlgebraicEvaluation = calculateEvaluation(position, algebraicEvaluator, mobilityCalculator);
		Assert.assertEquals(firstAlgebraicEvaluation, secondAlgebraicEvaluation);

		final Supplier<IPositionEvaluation> coeffCountEvaluationFactory = AlgebraicPositionEvaluation.getCoeffCountTestingFactory();
		final IPositionEvaluator coeffCountEvaluator = new GeneralPositionEvaluator(coeffCountEvaluationFactory);

		final int firstCoeffCountEvaluation = calculateEvaluation(position, coeffCountEvaluator, mobilityCalculator);
		final int secondCoeffCountEvaluation = calculateEvaluation(position, coeffCountEvaluator, mobilityCalculator);
		Assert.assertEquals(firstCoeffCountEvaluation, secondCoeffCountEvaluation);

		Assert.assertEquals(firstAlgebraicEvaluation, firstCoeffCountEvaluation);

		return firstAlgebraicEvaluation;
	}

	@Test
	public void testConsistency() throws IOException {
		for (String positionFen: TESTED_POSITIONS) {
			final Fen fen = new Fen();
			fen.readFenFromString(positionFen);

			final Position position = fen.getPosition();
			final int originalEvaluation = testPositionConsistency(position);

			final Position mirrorPosition = new Position();
			mirrorPosition.assign(new MirrorPosition(position));
			final int mirrorEvaluation = testPositionConsistency(mirrorPosition);

			Assert.assertTrue(Math.abs(originalEvaluation + mirrorEvaluation) <= 2);
		}
	}
}
