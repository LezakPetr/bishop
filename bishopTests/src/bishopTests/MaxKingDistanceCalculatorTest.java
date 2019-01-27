package bishopTests;

import bishop.base.BitBoard;
import bishop.base.Square;
import bishop.engine.MaxKingDistanceCalculator;
import org.junit.Assert;
import org.junit.Test;

public class MaxKingDistanceCalculatorTest {

	private void testGetMaxKingDistance(final int square, final String mask, final int expectedDistance) {
		final int givenDistance = MaxKingDistanceCalculator.getMaxKingDistance(square, BitBoard.fromString(mask));

		Assert.assertEquals(expectedDistance, givenDistance);
	}

	@Test
	public void getMaxKingDistanceTest() {
		testGetMaxKingDistance(Square.E4, "a1, a8, h1, h8", 4);
		testGetMaxKingDistance(Square.E5, "a1, a8, h1, h8", 4);
		testGetMaxKingDistance(Square.F3, "a1, a8, h1, h8", 5);
		testGetMaxKingDistance(Square.G2, "a1, a8, h1, h8", 6);
		testGetMaxKingDistance(Square.H1, "a1, a8, h1, h8", 7);
		testGetMaxKingDistance(Square.D1, "d1", 0);
		testGetMaxKingDistance(Square.D2, "d1, e1", 1);
		testGetMaxKingDistance(Square.E3, "d1, e1", 2);
		testGetMaxKingDistance(Square.B4, "d1, e1", 3);
	}
}
