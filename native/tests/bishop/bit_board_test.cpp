
#include "../../bishop/base/bit_board.h"
#include "../../bishop/base/color.h"
#include "../test_base/test_base.h"

#include <random>

using namespace bishop::base;


TEST (testGetSquareMask) {
	BitBoard::Type mask = BitBoard::EMPTY;
	
	for (Square::Type square = Square::FIRST; square < Square::LAST; square++)
		mask ^= BitBoard::getSquareMask (square);

	assertEquals(BitBoard::FULL, mask);
}

TEST (testOf0) {
	assertEquals(BitBoard::EMPTY, BitBoard::of());
}

TEST (testOf1) {
	for (Square::Type s1 = Square::FIRST; s1 < Square::LAST; s1++)
		assertEquals(BitBoard::getSquareMask(s1), BitBoard::of(s1));
}

TEST (testOf2) {
	for (Square::Type s1 = Square::FIRST; s1 < Square::LAST; s1++) {
		for (Square::Type s2 = Square::FIRST; s2 < Square::LAST; s2++) {
			assertEquals(
				BitBoard::getSquareMask(s1) | BitBoard::getSquareMask(s2),
				BitBoard::of(s1, s2)
			);
		}
	}
}

TEST (testOf3) {
	for (Square::Type s1 = Square::FIRST; s1 < Square::LAST; s1++) {
		for (Square::Type s2 = Square::FIRST; s2 < Square::LAST; s2++) {
			for (Square::Type s3 = Square::FIRST; s3 < Square::LAST; s3++) {
				assertEquals(
						BitBoard::getSquareMask(s1) | BitBoard::getSquareMask(s2) | BitBoard::getSquareMask(s3),
						BitBoard::of(s1, s2, s3)
				);
			}
		}
	}
}

TEST (testOf4) {
	for (Square::Type s1 = Square::FIRST; s1 < Square::LAST; s1++) {
		for (Square::Type s2 = Square::FIRST; s2 < Square::LAST; s2++) {
			for (Square::Type s3 = Square::FIRST; s3 < Square::LAST; s3++) {
				for (Square::Type s4 = Square::FIRST; s4 < Square::LAST; s4++){
					assertEquals(
						BitBoard::getSquareMask(s1) | BitBoard::getSquareMask(s2) | BitBoard::getSquareMask(s3) | BitBoard::getSquareMask(s4),
						BitBoard::of(s1, s2, s3, s4)
					);
				}
			}
		}
	}
}

TEST (testGetSquareCount) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution;
		
	for (int i = 0; i < 1000000; i++) {
		BitBoard::Type mask = BitBoard::EMPTY;
		int expectedCount = 0;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			if (squareDistribution(rng)) {
				mask |= BitBoard::of(square);
				expectedCount++;
			}
		}

		assertEquals(expectedCount, BitBoard::getSquareCount(mask));
		assertEquals(expectedCount, BitBoard::getSquareCountSparse(mask));
	}

	assertEquals(0, BitBoard::getSquareCount(BitBoard::EMPTY));
	assertEquals(Square::COUNT, BitBoard::getSquareCount(BitBoard::FULL));

	assertEquals(0, BitBoard::getSquareCountSparse(BitBoard::EMPTY));
	assertEquals(Square::COUNT, BitBoard::getSquareCountSparse(BitBoard::FULL));
}
/*
TEST (testGetSquareCountNearKing) {
	::std::minstd_rand rng;
	::std::uniform_int_distribution<Square::Type> kingDistribution (Square::FIRST, Square::LAST - 1);
	::std::bernoulli_distribution squareDistribution;

	for (int i = 0; i < 1000000; i++) {
		const Square::Type kingSquare = kingDistribution(rng);
		const BitBoard::Type nearKingMask = BoardConstants::getKingNearSquares(kingSquare);

		BitBoard::Type mask = BitBoard::EMPTY;
		int expectedCount = 0;

		for (BitLoop loop(nearKingMask); loop.hasNextSquare(); ) {
			Square::Type square = loop.getNextSquare();
			
			if (squareDistribution(rng)) {
				mask |= BitBoard::of(square);
				expectedCount++;
			}
		}

		assertEquals(expectedCount, BitBoard::getSquareCountNearKing(mask));
		assertEquals(BitBoard::getSquareCount(nearKingMask), BitBoard::getSquareCountNearKing(nearKingMask));
	}

	assertEquals(0, BitBoard::getSquareCountNearKing(BitBoard::EMPTY));
}
*/
TEST (testHasSingleSquare) {
	// Empty board
	assertFalse(BitBoard::hasSingleSquare(BitBoard::EMPTY));

	// Single square
	for (int s = Square::FIRST; s < Square::LAST; s++)
		assertTrue(BitBoard::hasSingleSquare(BitBoard::of(s)));

	// Two squares
	for (int s1 = Square::FIRST; s1 < Square::LAST; s1++) {
		for (int s2 = s1 + 1; s2 < Square::LAST; s2++)
			assertFalse(BitBoard::hasSingleSquare(BitBoard::of(s1, s2)));
	}
}

TEST (testHasAtLeastTwoSquares) {
	// Empty board
	assertFalse(BitBoard::hasAtLeastTwoSquares(BitBoard::EMPTY));

	// Single square
	for (int s = Square::FIRST; s < Square::LAST; s++)
		assertFalse(BitBoard::hasAtLeastTwoSquares(BitBoard::of(s)));

	// Two squares
	for (int s1 = Square::FIRST; s1 < Square::LAST; s1++) {
		for (int s2 = s1 + 1; s2 < Square::LAST; s2++)
			assertTrue(BitBoard::hasAtLeastTwoSquares(BitBoard::of(s1, s2)));
	}
}

static BitBoard::Type getRandomBoard(::std::minstd_rand &rng, const Square::Type firstSquare, const Square::Type lastSquare) {
	::std::bernoulli_distribution squareDistribution;
	BitBoard::Type mask = BitBoard::EMPTY;

	for (Square::Type square = firstSquare; square < lastSquare; square++) {
		if (squareDistribution(rng))
			mask |= BitBoard::of(square);
	}

	return mask;
}

TEST (testGetFirstSquare) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution;

	for (Square::Type firstSquare = Square::FIRST; firstSquare < Square::LAST; firstSquare++) {
		BitBoard::Type mask = BitBoard::of(firstSquare);

		mask |= getRandomBoard(rng, firstSquare + 1, Square::LAST);

		assertEquals(firstSquare, BitBoard::getFirstSquare(mask));
	}

	assertEquals(Square::NONE, BitBoard::getFirstSquare(BitBoard::EMPTY));
}

TEST (testMirrorBoard) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution;

	for (int i = 0; i < 100000; i++) {
		BitBoard::Type mask = BitBoard::EMPTY;
		BitBoard::Type mirrorMask = BitBoard::EMPTY;

		for (File::Type file = File::FIRST; file < File::LAST; file++) {
			for (Rank::Type rank = Rank::FIRST; rank < Rank::LAST; rank++) {
				if (squareDistribution (rng)) {
					mask |= BitBoard::of(Square::onFileRank(file, rank));
					mirrorMask |= BitBoard::of(Square::onFileRank(file, Rank::getOppositeRank(rank)));
				}
			}
		}

		assertEquals(mirrorMask, BitBoard::getMirrorBoard(mask));
		assertEquals(mask, BitBoard::getMirrorBoard(mirrorMask));
	}
}

TEST (testGetNthSquare) {
	::std::minstd_rand rng;
	::std::uniform_int_distribution<Square::Type> expectedSquareDistribution (Square::FIRST, Square::LAST - 1);

	for (int i = 0; i < 100000; i++) {
		const Square::Type expectedSquare = expectedSquareDistribution (rng);
		const BitBoard::Type lowerMask = getRandomBoard(rng, Square::FIRST, expectedSquare);
		const BitBoard::Type upperMask = getRandomBoard(rng, expectedSquare + 1, Square::LAST);
		const BitBoard::Type mask = lowerMask | BitBoard::of(expectedSquare) | upperMask;

		assertEquals(expectedSquare, BitBoard::getNthSquare(mask, BitBoard::getSquareCount(lowerMask)));
	}
}

TEST (testGetSquareIndex) {
	::std::minstd_rand rng;
	::std::uniform_int_distribution<Square::Type> squareDistribution (Square::FIRST, Square::LAST - 1);

	for (int i = 0; i < 100000; i++) {
		const Square::Type square = squareDistribution(rng);
		const BitBoard::Type lowerMask = getRandomBoard(rng, Square::FIRST, square);
		const BitBoard::Type upperMask = getRandomBoard(rng, square + 1, Square::LAST);
		const BitBoard::Type mask = lowerMask | BitBoard::of(square) | upperMask;

		assertEquals(BitBoard::getSquareCount(lowerMask), BitBoard::getSquareIndex(mask, square));
	}
}

TEST (testExtendForward) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution;

	for (int i = 0; i < 100000; i++) {
		BitBoard::Type mask = BitBoard::EMPTY;
		BitBoard::Type extendedMask = BitBoard::EMPTY;
		BitBoard::Type extendedWithoutItself = BitBoard::EMPTY;

		for (File::Type file = File::FIRST; file < File::LAST; file++) {
			bool extended = false;

			for (Rank::Type rank = Rank::FIRST; rank < Rank::LAST; rank++) {
				const Square::Type square = Square::onFileRank(file, rank);

				if (extended)
					extendedWithoutItself |= BitBoard::of(square);

				if (squareDistribution (rng)) {
					mask |= BitBoard::of(square);
					extended = true;
				}

				if (extended)
					extendedMask |= BitBoard::of(square);
			}
		}

		assertEquals(extendedMask, BitBoard::extendForward(mask));
		assertEquals(extendedMask, BitBoard::extendForwardByColor(Color::WHITE, mask));
		assertEquals(extendedWithoutItself, BitBoard::extendForwardByColorWithoutItself(Color::WHITE, mask));
	}
}


TEST (testExtendBackward) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution;

	for (int i = 0; i < 100000; i++) {
		BitBoard::Type mask = BitBoard::EMPTY;
		BitBoard::Type extendedMask = BitBoard::EMPTY;
		BitBoard::Type extendedWithoutItself = BitBoard::EMPTY;

		for (File::Type file = File::FIRST; file < File::LAST; file++) {
			bool extended = false;

			for (Rank::Type rank = Rank::LAST - 1; rank >= Rank::FIRST; rank--) {
				const Square::Type square = Square::onFileRank(file, rank);

				if (extended)
					extendedWithoutItself |= BitBoard::of(square);

				if (squareDistribution(rng)) {
					mask |= BitBoard::of(square);
					extended = true;
				}

				if (extended)
					extendedMask |= BitBoard::of(square);
			}
		}

		assertEquals(extendedMask, BitBoard::extendBackward(mask));
		assertEquals(extendedMask, BitBoard::extendForwardByColor(Color::BLACK, mask));
		assertEquals(extendedWithoutItself, BitBoard::extendForwardByColorWithoutItself(Color::BLACK, mask));
	}
}

TEST (testContainsSquare) {
	::std::minstd_rand rng;

	for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
		const BitBoard::Type lowerMask = getRandomBoard(rng, Square::FIRST, square);
		const BitBoard::Type upperMask = getRandomBoard(rng, square + 1, Square::LAST);

		assertFalse(BitBoard::containsSquare(lowerMask | upperMask, square));
		assertTrue(BitBoard::containsSquare(lowerMask | upperMask | BitBoard::of(square), square));
	}
}

TEST_RUNNER_BEGIN
	RUN_TEST(testGetSquareMask);
	RUN_TEST(testOf0);
	RUN_TEST(testOf1);
	RUN_TEST(testOf2);
	RUN_TEST(testOf3);
	RUN_TEST(testOf4);
	RUN_TEST(testGetSquareCount);
//	RUN_TEST(testGetSquareCountNearKing);
	RUN_TEST(testHasSingleSquare);
	RUN_TEST(testHasAtLeastTwoSquares);
	RUN_TEST(testGetFirstSquare);
	RUN_TEST(testMirrorBoard);
	RUN_TEST(testGetNthSquare);
	RUN_TEST(testGetSquareIndex);
TEST_RUNNER_END

