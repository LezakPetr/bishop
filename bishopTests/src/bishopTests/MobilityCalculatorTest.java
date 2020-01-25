package bishopTests;

import bishop.base.Color;
import bishop.base.Fen;
import bishop.base.Position;
import bishop.engine.MobilityCalculator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.function.BiPredicate;

public class MobilityCalculatorTest {
	class TestCase {
		public final String positionFen;
		public final boolean expectedValue;

		public TestCase(final String positionFen, final boolean expectedValue) {
			this.positionFen = positionFen;
			this.expectedValue = expectedValue;
		}
	}

	private void testPredicate(final BiPredicate<MobilityCalculator, Position> predicate, final TestCase[] testCaseArray) throws IOException {
		final Fen fen = new Fen();

		for (TestCase testCase: testCaseArray) {
			fen.readFenFromString(testCase.positionFen);

			final Position position = fen.getPosition();
			final MobilityCalculator mobilityCalculator = new MobilityCalculator();
			mobilityCalculator.calculate(position);

			Assert.assertEquals(testCase.positionFen, testCase.expectedValue, predicate.test(mobilityCalculator, position));
		}
	}

	@Test
	public void canBeMateTest() throws IOException {
		final TestCase[] testValueArray = {
			new TestCase("3N1K2/5q2/8/8/2b5/8/8/7k w - - 0 1", true),
			new TestCase("3N1K2/5q2/8/8/8/8/8/7k w - - 0 1", false),
			new TestCase("2R3k1/5p1p/6p1/8/4r3/2B5/8/7K b - - 0 1", true),
			new TestCase("2R3k1/5p1p/6p1/4r3/8/2B5/8/7K b - - 0 1", false),
			new TestCase("2R3k1/5p1N/6p1/8/4r3/2B5/8/7K b - - 0 1", false),
			new TestCase("2R3k1/5ppp/8/8/8/8/8/7K b - - 0 1", true)
		};

		testPredicate(MobilityCalculator::canBeMate, testValueArray);
	}

	@Test
	public void speedTest() {
		final int count = 20000000;

		final MobilityCalculator mobilityCalculator = new MobilityCalculator();
		final Position position = new Position();
		position.setInitialPosition();

		for (int i = 0; i < count; i++)
			mobilityCalculator.calculate(position);

		final long t1 = System.currentTimeMillis();

		for (int i = 0; i < count; i++)
			mobilityCalculator.calculate(position);

		final long t2 = System.currentTimeMillis();

		final double iterPerSec = (double) count * 1000 / (t2 - t1);

		System.out.println("Mobility calculator: " + iterPerSec);
	}

	@Test
	public void isDoubleCheckTest() throws IOException {
		final TestCase[] testValueArray = {
				new TestCase("2k5/1P6/8/8/8/8/8/3K4 b - - 0 1", false),   // Single check
				new TestCase("2k5/1N6/Q2N4/8/8/8/8/3K4 b - - 0 1", false),   // Single check
				new TestCase("5k2/8/4N3/8/8/5Q2/8/3K4 b - - 0 1", true),   // Double check Q+N
				new TestCase("5k2/8/3B4/8/8/5Q2/8/3K4 b - - 0 1", true),   // Double check Q+B
				new TestCase("5k2/8/5R2/8/1Q6/8/8/3K4 b - - 0 1", true),   // Double check Q+R
				new TestCase("5k2/8/3r4/8/8/5b2/8/3K4 w - - 0 1", true),   // Double check R+B
				new TestCase("5k2/8/3r4/8/8/4n3/8/3K4 w - - 0 1", true),   // Double check R+N
				new TestCase("5k2/6b1/8/8/4n3/2K5/8/8 w - - 0 1", true),   // Double check B+N
				new TestCase("5k2/2r5/8/8/3p4/2K5/8/8 w - - 0 1", true),   // Double check R+P
				new TestCase("5k2/2q5/8/8/3p4/2K5/8/8 w - - 0 1", true)   // Double check Q+P
		};

		testPredicate(
				(m, p) -> m.isDoubleCheck(
						Color.getOppositeColor(p.getOnTurn()),
						p.getKingPosition(p.getOnTurn())
				),
				testValueArray
		);
	}
}
