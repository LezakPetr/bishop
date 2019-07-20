

#include "../../bishop/base/bit_board_combinator.h"
#include "../test_base/test_base.h"

#include <vector>

using namespace bishop::base;


std::vector<std::vector<Square::Type>> TESTED_MASKS = {
	{Square::A1, Square::B1, Square::H8},
	{Square::B5, Square::C5, Square::D6, Square::E6}
};

TEST (test) {
	for (const std::vector<Square::Type> &squares: TESTED_MASKS) {
		BitBoardCombinator combinator(BitBoard::fromRange(squares.begin(), squares.end()));

		for (unsigned int i = 0; i < (1ULL << squares.size()); i++) {
			BitBoard::Type expectedCombination = BitBoard::EMPTY;

			for (size_t j = 0; j < squares.size(); j++) {
				const Square::Type square = squares[j];

				expectedCombination |= (BitBoard::Type) ((i >> j) & 0x01L) << square;
			}

			assertTrue(combinator.hasNextCombination());
			assertEquals(expectedCombination, combinator.getNextCombination());
		}

		assertFalse(combinator.hasNextCombination());
	}
}

TEST_RUNNER_BEGIN
	RUN_TEST(test);
TEST_RUNNER_END
