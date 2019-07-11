

#include "../../bishop/base/between_table.h"
#include "../../bishop/base/square.h"
#include "../../bishop/base/bit_board.h"
#include "../test_base/test_base.h"


using namespace bishop::base;


struct TestValue {
	Square::Type square1;
	Square::Type square2;
	BitBoard::Type expectedMask;
};

static const TestValue TEST_VALUES[] = {
	{ Square::A1, Square::H8, BitBoard::of (Square::B2, Square::C3, Square::D4, Square::E5, Square::F6, Square::G7)},
	{ Square::B3, Square::B6, BitBoard::of (Square::B4, Square::B5) },
	{ Square::C2, Square::H2, BitBoard::of (Square::D2, Square::E2, Square::F2, Square::G2) },
	{ Square::A8, Square::H1, BitBoard::of (Square::B7, Square::C6, Square::D5, Square::E4, Square::F3, Square::G2) },
	{ Square::E4, Square::E5, BitBoard::EMPTY },
	{ Square::E4, Square::F6, BitBoard::EMPTY }
};

TEST (tableCellTest) {
	for (TestValue testValue: TEST_VALUES) {
		const BitBoard::Type expectedMask = testValue.expectedMask;
		
		assertEquals (expectedMask, BetweenTable::getItem (testValue.square1, testValue.square2));
		assertEquals (expectedMask, BetweenTable::getItem (testValue.square2, testValue.square1));
	}
}

TEST_RUNNER_BEGIN
	RUN_TEST(tableCellTest);
TEST_RUNNER_END

