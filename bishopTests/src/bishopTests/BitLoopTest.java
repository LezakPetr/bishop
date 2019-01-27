package bishopTests;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.Square;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.SplittableRandom;

public class BitLoopTest {
	@Test
	public void testCorrectResult() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			final Set<Integer> expectedSquares = new HashSet<>();
			long board = BitBoard.EMPTY;

			for (int square = Square.FIRST; square < Square.LAST; square++) {
				if (rng.nextBoolean()) {
					expectedSquares.add(square);
					board |= BitBoard.of(square);
				}
			}

			final Set<Integer> givenSquares = new HashSet<>();

			for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); )
				givenSquares.add(loop.getNextSquare());

			Assert.assertEquals(expectedSquares, givenSquares);
		}
	}
}
