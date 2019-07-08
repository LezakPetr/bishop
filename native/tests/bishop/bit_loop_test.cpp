
#include "../../bishop/base/bit_board.h"
#include "../../bishop/base/bit_loop.h"
#include "../test_base/test_base.h"

#include <random>
#include <set>

using namespace bishop::base;


TEST (testCorrectResult) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution;

	for (int i = 0; i < 100000; i++) {
		::std::set<Square::Type> expectedSquares;
		BitBoard::Type board = BitBoard::EMPTY;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			if (squareDistribution(rng)) {
				expectedSquares.insert(square);
				board |= BitBoard::of(square);
			}
		}

		::std::set<Square::Type> givenSquares;

		for (BitLoop loop(board); loop.hasNextSquare(); )
			givenSquares.insert(loop.getNextSquare());

		assertTrue(expectedSquares == givenSquares);
	}
}


TEST_RUNNER_BEGIN
	RUN_TEST(testCorrectResult);
TEST_RUNNER_END

