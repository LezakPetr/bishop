

#include "../../bishop/base/castling_constants.h"
#include "../../bishop/base/between_table.h"
#include "../test_base/test_base.h"


using namespace bishop::base;


TEST(getCastlingMiddleSquareMaskTest) {
	assertEquals(BetweenTable::getItem(Square::E1, Square::H1), CastlingConstants::of(Color::WHITE, CastlingType::SHORT).getMiddleSquareMask());
	assertEquals(BetweenTable::getItem(Square::E1, Square::A1), CastlingConstants::of(Color::WHITE, CastlingType::LONG).getMiddleSquareMask());
	assertEquals(BetweenTable::getItem(Square::E8, Square::H8), CastlingConstants::of(Color::BLACK, CastlingType::SHORT).getMiddleSquareMask());
	assertEquals(BetweenTable::getItem(Square::E8, Square::A8), CastlingConstants::of(Color::BLACK, CastlingType::LONG).getMiddleSquareMask());
}

TEST(getCastlingRookBeginSquareTest) {
	assertEquals(Square::H1, CastlingConstants::of(Color::WHITE, CastlingType::SHORT).getRookBeginSquare());
	assertEquals(Square::A1, CastlingConstants::of(Color::WHITE, CastlingType::LONG).getRookBeginSquare());
	assertEquals(Square::H8, CastlingConstants::of(Color::BLACK, CastlingType::SHORT).getRookBeginSquare());
	assertEquals(Square::A8, CastlingConstants::of(Color::BLACK, CastlingType::LONG).getRookBeginSquare());
}

TEST(getCastlingRookTargetSquareTest) {
	assertEquals(Square::F1, CastlingConstants::of(Color::WHITE, CastlingType::SHORT).getRookTargetSquare());
	assertEquals(Square::D1, CastlingConstants::of(Color::WHITE, CastlingType::LONG).getRookTargetSquare());
	assertEquals(Square::F8, CastlingConstants::of(Color::BLACK, CastlingType::SHORT).getRookTargetSquare());
	assertEquals(Square::D8, CastlingConstants::of(Color::BLACK, CastlingType::LONG).getRookTargetSquare());
}

TEST(getCastlingKingTargetSquareTest) {
	assertEquals(Square::G1, CastlingConstants::of(Color::WHITE, CastlingType::SHORT).getKingTargetSquare());
	assertEquals(Square::C1, CastlingConstants::of(Color::WHITE, CastlingType::LONG).getKingTargetSquare());
	assertEquals(Square::G8, CastlingConstants::of(Color::BLACK, CastlingType::SHORT).getKingTargetSquare());
	assertEquals(Square::C8, CastlingConstants::of(Color::BLACK, CastlingType::LONG).getKingTargetSquare());
}

TEST(getCastlingKingMiddleSquareTest) {
	assertEquals(Square::F1, CastlingConstants::of(Color::WHITE, CastlingType::SHORT).getKingMiddleSquare());
	assertEquals(Square::D1, CastlingConstants::of(Color::WHITE, CastlingType::LONG).getKingMiddleSquare());
	assertEquals(Square::F8, CastlingConstants::of(Color::BLACK, CastlingType::SHORT).getKingMiddleSquare());
	assertEquals(Square::D8, CastlingConstants::of(Color::BLACK, CastlingType::LONG).getKingMiddleSquare());
}

TEST(getCastlingKingChangeMaskTest) {
	assertEquals(BitBoard::of(Square::E1, Square::G1), CastlingConstants::of(Color::WHITE, CastlingType::SHORT).getKingChangeMask());
	assertEquals(BitBoard::of(Square::E1, Square::C1), CastlingConstants::of(Color::WHITE, CastlingType::LONG).getKingChangeMask());
	assertEquals(BitBoard::of(Square::E8, Square::G8), CastlingConstants::of(Color::BLACK, CastlingType::SHORT).getKingChangeMask());
	assertEquals(BitBoard::of(Square::E8, Square::C8), CastlingConstants::of(Color::BLACK, CastlingType::LONG).getKingChangeMask());
}

TEST(getCastlingRookChangeMaskTest) {
	assertEquals(BitBoard::of(Square::H1, Square::F1), CastlingConstants::of(Color::WHITE, CastlingType::SHORT).getRookChangeMask());
	assertEquals(BitBoard::of(Square::A1, Square::D1), CastlingConstants::of(Color::WHITE, CastlingType::LONG).getRookChangeMask());
	assertEquals(BitBoard::of(Square::H8, Square::F8), CastlingConstants::of(Color::BLACK, CastlingType::SHORT).getRookChangeMask());
	assertEquals(BitBoard::of(Square::A8, Square::D8), CastlingConstants::of(Color::BLACK, CastlingType::LONG).getRookChangeMask());
}

TEST_RUNNER_BEGIN
	RUN_TEST(getCastlingMiddleSquareMaskTest);
	RUN_TEST(getCastlingRookBeginSquareTest);
	RUN_TEST(getCastlingRookTargetSquareTest);
	RUN_TEST(getCastlingKingTargetSquareTest);
	RUN_TEST(getCastlingKingMiddleSquareTest);
	RUN_TEST(getCastlingKingChangeMaskTest);
	RUN_TEST(getCastlingRookChangeMaskTest);
TEST_RUNNER_END

