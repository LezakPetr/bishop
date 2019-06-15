package bishopTests;

import bishop.base.Color;
import org.junit.Assert;
import org.junit.Test;

import java.util.SplittableRandom;

public class ColorTest {
	@Test
	public void testIsValid() {
		Assert.assertTrue(Color.isValid(Color.WHITE));
		Assert.assertTrue(Color.isValid(Color.BLACK));
		Assert.assertFalse(Color.isValid(Color.LAST));
		Assert.assertFalse(Color.isValid(Color.FIRST - 1));
	}

	@Test
	public void testGetOppositeColor() {
		Assert.assertEquals(Color.BLACK, Color.getOppositeColor(Color.WHITE));
		Assert.assertEquals(Color.WHITE, Color.getOppositeColor(Color.BLACK));
	}

	private static long colorToBitBoardMapper(final int color) {
		return 8941646 + 16498606646164L * color;
	}

	@Test
	public void mapToBitBoardArray() {
		final long[] result = Color.mapToBitBoardArray(ColorTest::colorToBitBoardMapper);

		Assert.assertEquals(Color.LAST, result.length);

		for (int c = Color.FIRST; c < Color.LAST; c++)
			Assert.assertEquals(colorToBitBoardMapper(c), result[c]);
	}

	@Test
	public void testColorNegate() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 1000000; i++) {
			final int value = rng.nextInt();

			Assert.assertEquals(value, Color.colorNegate(Color.WHITE, value));
			Assert.assertEquals(-value, Color.colorNegate(Color.BLACK, value));
		}
	}

	@Test
	public void testColorNegateLong() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 1000000; i++) {
			final long value = rng.nextLong();

			Assert.assertEquals(value, Color.colorNegate(Color.WHITE, value));
			Assert.assertEquals(-value, Color.colorNegate(Color.BLACK, value));
		}
	}
}
