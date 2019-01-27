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
	}
}
