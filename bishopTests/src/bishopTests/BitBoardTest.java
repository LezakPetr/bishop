package bishopTests;

import bishop.base.*;
import org.junit.Assert;

import org.junit.Test;

import java.util.SplittableRandom;
import java.util.stream.IntStream;

public class BitBoardTest {

	@Test
	public void testGetSquareMask()
	{
		final long mask = IntStream.range(Square.FIRST, Square.LAST)
				.mapToLong(BitBoard::getSquareMask)
				.reduce(BitBoard.EMPTY, (a, b) -> a ^ b);

		Assert.assertEquals(BitBoard.FULL, mask);
	}

	@Test
	public void testOf0()
	{
		Assert.assertEquals(BitBoard.EMPTY, BitBoard.of());
	}

	@Test
	public void testOf1()
	{
		for (int s1 = Square.FIRST; s1 < Square.LAST; s1++) {
			Assert.assertEquals(BitBoard.getSquareMask(s1), BitBoard.of(s1));
		}
	}

	@Test
	public void testOf2()
	{
		for (int s1 = Square.FIRST; s1 < Square.LAST; s1++) {
			for (int s2 = Square.FIRST; s2 < Square.LAST; s2++) {
				Assert.assertEquals(
						BitBoard.getSquareMask(s1) | BitBoard.getSquareMask(s2),
						BitBoard.of(s1, s2)
				);
			}
		}
	}

	@Test
	public void testOf3()
	{
		for (int s1 = Square.FIRST; s1 < Square.LAST; s1++) {
			for (int s2 = Square.FIRST; s2 < Square.LAST; s2++) {
				for (int s3 = Square.FIRST; s3 < Square.LAST; s3++) {
					Assert.assertEquals(
							BitBoard.getSquareMask(s1) | BitBoard.getSquareMask(s2) | BitBoard.getSquareMask(s3),
							BitBoard.of(s1, s2, s3)
					);
				}
			}
		}
	}

	@Test
	public void testOf4()
	{
		for (int s1 = Square.FIRST; s1 < Square.LAST; s1++) {
			for (int s2 = Square.FIRST; s2 < Square.LAST; s2++) {
				for (int s3 = Square.FIRST; s3 < Square.LAST; s3++) {
					for (int s4 = Square.FIRST; s4 < Square.LAST; s4++) {
						Assert.assertEquals(
								BitBoard.getSquareMask(s1) | BitBoard.getSquareMask(s2) | BitBoard.getSquareMask(s3) | BitBoard.getSquareMask(s4),
								BitBoard.of(s1, s2, s3, s4)
						);
					}
				}
			}
		}
	}

	@Test
	public void testGetSquareCount() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 1000000; i++) {
			long mask = BitBoard.EMPTY;
			int expectedCount = 0;

			for (int square = Square.FIRST; square < Square.LAST; square++) {
				if (rng.nextBoolean()) {
					mask |= BitBoard.of(square);
					expectedCount++;
				}
			}

			Assert.assertEquals(expectedCount, BitBoard.getSquareCount(mask));
			Assert.assertEquals(expectedCount, BitBoard.getSquareCountSparse(mask));
		}

		Assert.assertEquals(0, BitBoard.getSquareCount(BitBoard.EMPTY));
		Assert.assertEquals(Square.COUNT, BitBoard.getSquareCount(BitBoard.FULL));

		Assert.assertEquals(0, BitBoard.getSquareCountSparse(BitBoard.EMPTY));
		Assert.assertEquals(Square.COUNT, BitBoard.getSquareCountSparse(BitBoard.FULL));
	}

	@Test
	public void testHasSingleSquare() {
		// Empty board
		Assert.assertFalse(BitBoard.hasSingleSquare(BitBoard.EMPTY));

		// Single square
		for (int s = Square.FIRST; s < Square.LAST; s++)
			Assert.assertTrue(BitBoard.hasSingleSquare(BitBoard.of(s)));

		// Two squares
		for (int s1 = Square.FIRST; s1 < Square.LAST; s1++) {
			for (int s2 = s1 + 1; s2 < Square.LAST; s2++)
				Assert.assertFalse(BitBoard.hasSingleSquare(BitBoard.of(s1, s2)));
		}
	}

	@Test
	public void testHasAtLeastTwoSquares() {
		// Empty board
		Assert.assertFalse(BitBoard.hasAtLeastTwoSquares(BitBoard.EMPTY));

		// Single square
		for (int s = Square.FIRST; s < Square.LAST; s++)
			Assert.assertFalse(BitBoard.hasAtLeastTwoSquares(BitBoard.of(s)));

		// Two squares
		for (int s1 = Square.FIRST; s1 < Square.LAST; s1++) {
			for (int s2 = s1 + 1; s2 < Square.LAST; s2++)
				Assert.assertTrue(BitBoard.hasAtLeastTwoSquares(BitBoard.of(s1, s2)));
		}
	}

	@Test
	public void testGetFirstSquare() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int firstSquare = Square.FIRST; firstSquare < Square.LAST; firstSquare++) {
			long mask = BitBoard.of(firstSquare);

			mask |= getRandomBoard(rng, firstSquare + 1, Square.LAST);

			Assert.assertEquals(firstSquare, BitBoard.getFirstSquare(mask));
		}

		Assert.assertEquals(Square.NONE, BitBoard.getFirstSquare(BitBoard.EMPTY));
	}

	@Test
	public void testGetLastSquare() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int lastSquare = Square.FIRST; lastSquare < Square.LAST; lastSquare++) {
			long mask = BitBoard.of(lastSquare);

			mask |= getRandomBoard(rng, Square.FIRST, lastSquare);

			Assert.assertEquals(lastSquare, BitBoard.getLastSquare(mask));
		}

		Assert.assertEquals(Square.NONE, BitBoard.getLastSquare(BitBoard.EMPTY));
	}

	private long getRandomBoard(final SplittableRandom rng, final int firstSquare, final int lastSquare) {
		long mask = BitBoard.EMPTY;

		for (int prevSquare = firstSquare; prevSquare < lastSquare; prevSquare++) {
			if (rng.nextBoolean())
				mask |= BitBoard.of(prevSquare);
		}

		return mask;
	}

	@Test
	public void testMirrorBoard() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			long mask = BitBoard.EMPTY;
			long mirrorMask = BitBoard.EMPTY;

			for (int file = File.FIRST; file < File.LAST;file++) {
				for (int rank = Rank.FIRST; rank < Rank.LAST; rank++) {
					if (rng.nextBoolean()) {
						mask |= BitBoard.of(Square.onFileRank(file, rank));
						mirrorMask |= BitBoard.of(Square.onFileRank(file, Rank.getOppositeRank(rank)));
					}
				}
			}

			Assert.assertEquals(mirrorMask, BitBoard.getMirrorBoard(mask));
			Assert.assertEquals(mask, BitBoard.getMirrorBoard(mirrorMask));
		}
	}

	@Test
	public void testGetNthSquare() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			final int expectedSquare = rng.nextInt(Square.FIRST, Square.LAST);
			final long lowerMask = getRandomBoard(rng, Square.FIRST, expectedSquare);
			final long upperMask = getRandomBoard(rng, expectedSquare + 1, Square.LAST);
			final long mask = lowerMask | BitBoard.of(expectedSquare) | upperMask;

			Assert.assertEquals(expectedSquare, BitBoard.getNthSquare(mask, BitBoard.getSquareCount(lowerMask)));
		}
	}

	@Test
	public void testGetSquareIndex() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			final int square = rng.nextInt(Square.FIRST, Square.LAST);
			final long lowerMask = getRandomBoard(rng, Square.FIRST, square);
			final long upperMask = getRandomBoard(rng, square + 1, Square.LAST);
			final long mask = lowerMask | BitBoard.of(square) | upperMask;

			Assert.assertEquals(BitBoard.getSquareCount(lowerMask), BitBoard.getSquareIndex(mask, square));
		}
	}

	@Test
	public void testExtendForward() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			long mask = BitBoard.EMPTY;
			long extendedMask = BitBoard.EMPTY;
			long extendedWithoutItself = BitBoard.EMPTY;

			for (int file = File.FIRST; file < File.LAST; file++) {
				boolean extended = false;

				for (int rank = Rank.FIRST; rank < Rank.LAST; rank++) {
					final int square = Square.onFileRank(file, rank);

					if (extended)
						extendedWithoutItself |= BitBoard.of(square);

					if (rng.nextBoolean()) {
						mask |= BitBoard.of(square);
						extended = true;
					}

					if (extended)
						extendedMask |= BitBoard.of(square);
				}
			}

			Assert.assertEquals(extendedMask, BitBoard.extendForward(mask));
			Assert.assertEquals(extendedMask, BitBoard.extendForwardByColor(Color.WHITE, mask));
			Assert.assertEquals(extendedWithoutItself, BitBoard.extendForwardByColorWithoutItself(Color.WHITE, mask));
		}
	}

	@Test
	public void testExtendBackward() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 100000; i++) {
			long mask = BitBoard.EMPTY;
			long extendedMask = BitBoard.EMPTY;
			long extendedWithoutItself = BitBoard.EMPTY;

			for (int file = File.FIRST; file < File.LAST; file++) {
				boolean extended = false;

				for (int rank = Rank.LAST - 1; rank >= Rank.FIRST; rank--) {
					final int square = Square.onFileRank(file, rank);

					if (extended)
						extendedWithoutItself |= BitBoard.of(square);

					if (rng.nextBoolean()) {
						mask |= BitBoard.of(square);
						extended = true;
					}

					if (extended)
						extendedMask |= BitBoard.of(square);
				}
			}

			Assert.assertEquals(extendedMask, BitBoard.extendBackward(mask));
			Assert.assertEquals(extendedMask, BitBoard.extendForwardByColor(Color.BLACK, mask));
			Assert.assertEquals(extendedWithoutItself, BitBoard.extendForwardByColorWithoutItself(Color.BLACK, mask));
		}
	}

	@Test
	public void testContainsSquare() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final long lowerMask = getRandomBoard(rng, Square.FIRST, square);
			final long upperMask = getRandomBoard(rng, square + 1, Square.LAST);

			Assert.assertFalse(BitBoard.containsSquare(lowerMask | upperMask, square));
			Assert.assertTrue(BitBoard.containsSquare(lowerMask | upperMask | BitBoard.of(square), square));
		}
	}

}
