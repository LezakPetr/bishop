package bishopTests;

import bishop.base.Fen;
import bishop.base.Position;
import bishop.engine.AttackCalculator;
import bishop.engine.MobilityCalculator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class MobilityCalculatorTest {

	@Test
	public void canBeMateTest() throws IOException {
		class TestValue {
			public final String positionFen;
			public final boolean canBeMate;

			public TestValue (final String positionFen, final boolean canBeMate) {
				this.positionFen = positionFen;
				this.canBeMate = canBeMate;
			}
		}

		TestValue[] testValueArray = {
			new TestValue("3N1K2/5q2/8/8/2b5/8/8/7k w - - 0 1", true),
			new TestValue("3N1K2/5q2/8/8/8/8/8/7k w - - 0 1", false),
			new TestValue("2R3k1/5p1p/6p1/8/4r3/2B5/8/7K b - - 0 1", true),
			new TestValue("2R3k1/5p1p/6p1/4r3/8/2B5/8/7K b - - 0 1", false),
			new TestValue("2R3k1/5p1N/6p1/8/4r3/2B5/8/7K b - - 0 1", false),
			new TestValue("2R3k1/5ppp/8/8/8/8/8/7K b - - 0 1", true)
		};

		final Fen fen = new Fen();

		for (TestValue testValue: testValueArray) {
			fen.readFenFromString(testValue.positionFen);

			final Position position = fen.getPosition();
			final MobilityCalculator mobilityCalculator = new MobilityCalculator();
			mobilityCalculator.calculate(position);

			Assert.assertEquals(testValue.canBeMate, mobilityCalculator.canBeMate(position));
		}
	}

}
