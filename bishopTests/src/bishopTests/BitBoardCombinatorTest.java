package bishopTests;

import bishop.base.BitBoard;
import bishop.base.BitBoardCombinator;
import bishop.base.Square;
import org.junit.Assert;
import org.junit.Test;

public class BitBoardCombinatorTest {
	private int[][] TESTED_MASKS = {
			{Square.A1, Square.B1, Square.H8},
			{Square.B5, Square.C5, Square.D6, Square.E6}
	};

	@Test
	public void test() {
		for (int[] squares: TESTED_MASKS) {
			final BitBoardCombinator combinator = new BitBoardCombinator(BitBoard.of(squares));

			for (int i = 0; i < (1 << squares.length); i++) {
				long expectedCombination = 0;

				for (int j = 0; j < squares.length; j++) {
					final int square = squares[j];

					expectedCombination |= ((i >>> j) & 0x01L) << square;
				}

				Assert.assertTrue(combinator.hasNextCombination());
				Assert.assertEquals(expectedCombination, combinator.getNextCombination());
			}

			Assert.assertFalse(combinator.hasNextCombination());
		}
	}
}
