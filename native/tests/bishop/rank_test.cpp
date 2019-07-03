

#include "../../bishop/base/rank.h"
#include "../test_base/test_base.h"

using namespace bishop::base;

TEST (getOppositeRankTest) {
	assertEquals(Rank::R8, Rank::getOppositeRank(Rank::R1));
	assertEquals(Rank::R7, Rank::getOppositeRank(Rank::R2));
	assertEquals(Rank::R6, Rank::getOppositeRank(Rank::R3));
	assertEquals(Rank::R5, Rank::getOppositeRank(Rank::R4));
	assertEquals(Rank::R4, Rank::getOppositeRank(Rank::R5));
	assertEquals(Rank::R3, Rank::getOppositeRank(Rank::R6));
	assertEquals(Rank::R2, Rank::getOppositeRank(Rank::R7));
	assertEquals(Rank::R1, Rank::getOppositeRank(Rank::R8));
}

TEST (getAbsoluteTest) {
	assertEquals(Rank::R1, Rank::getAbsolute(Rank::R1, Color::WHITE));
	assertEquals(Rank::R2, Rank::getAbsolute(Rank::R2, Color::WHITE));
	assertEquals(Rank::R3, Rank::getAbsolute(Rank::R3, Color::WHITE));
	assertEquals(Rank::R4, Rank::getAbsolute(Rank::R4, Color::WHITE));
	assertEquals(Rank::R5, Rank::getAbsolute(Rank::R5, Color::WHITE));
	assertEquals(Rank::R6, Rank::getAbsolute(Rank::R6, Color::WHITE));
	assertEquals(Rank::R7, Rank::getAbsolute(Rank::R7, Color::WHITE));
	assertEquals(Rank::R8, Rank::getAbsolute(Rank::R8, Color::WHITE));

	assertEquals(Rank::R8, Rank::getAbsolute(Rank::R1, Color::BLACK));
	assertEquals(Rank::R7, Rank::getAbsolute(Rank::R2, Color::BLACK));
	assertEquals(Rank::R6, Rank::getAbsolute(Rank::R3, Color::BLACK));
	assertEquals(Rank::R5, Rank::getAbsolute(Rank::R4, Color::BLACK));
	assertEquals(Rank::R4, Rank::getAbsolute(Rank::R5, Color::BLACK));
	assertEquals(Rank::R3, Rank::getAbsolute(Rank::R6, Color::BLACK));
	assertEquals(Rank::R2, Rank::getAbsolute(Rank::R7, Color::BLACK));
	assertEquals(Rank::R1, Rank::getAbsolute(Rank::R8, Color::BLACK));
}

TEST (getRelativeTest) {
	assertEquals(Rank::R1, Rank::getRelative(Rank::R1, Color::WHITE));
	assertEquals(Rank::R2, Rank::getRelative(Rank::R2, Color::WHITE));
	assertEquals(Rank::R3, Rank::getRelative(Rank::R3, Color::WHITE));
	assertEquals(Rank::R4, Rank::getRelative(Rank::R4, Color::WHITE));
	assertEquals(Rank::R5, Rank::getRelative(Rank::R5, Color::WHITE));
	assertEquals(Rank::R6, Rank::getRelative(Rank::R6, Color::WHITE));
	assertEquals(Rank::R7, Rank::getRelative(Rank::R7, Color::WHITE));
	assertEquals(Rank::R8, Rank::getRelative(Rank::R8, Color::WHITE));

	assertEquals(Rank::R8, Rank::getRelative(Rank::R1, Color::BLACK));
	assertEquals(Rank::R7, Rank::getRelative(Rank::R2, Color::BLACK));
	assertEquals(Rank::R6, Rank::getRelative(Rank::R3, Color::BLACK));
	assertEquals(Rank::R5, Rank::getRelative(Rank::R4, Color::BLACK));
	assertEquals(Rank::R4, Rank::getRelative(Rank::R5, Color::BLACK));
	assertEquals(Rank::R3, Rank::getRelative(Rank::R6, Color::BLACK));
	assertEquals(Rank::R2, Rank::getRelative(Rank::R7, Color::BLACK));
	assertEquals(Rank::R1, Rank::getRelative(Rank::R8, Color::BLACK));
}

TEST_RUNNER_BEGIN
	RUN_TEST(getOppositeRankTest);
	RUN_TEST(getAbsoluteTest);
	RUN_TEST(getRelativeTest);
TEST_RUNNER_END


