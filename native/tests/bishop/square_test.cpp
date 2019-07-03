
#include "../../bishop/base/square.h"
#include "../test_base/test_base.h"

using namespace bishop::base;

TEST (testOnFileRank) {
	assertEquals(Square::A1, Square::onFileRank(File::FA, Rank::R1));
	assertEquals(Square::B1, Square::onFileRank(File::FB, Rank::R1));
	assertEquals(Square::C1, Square::onFileRank(File::FC, Rank::R1));
	assertEquals(Square::D1, Square::onFileRank(File::FD, Rank::R1));
	assertEquals(Square::E1, Square::onFileRank(File::FE, Rank::R1));
	assertEquals(Square::F1, Square::onFileRank(File::FF, Rank::R1));
	assertEquals(Square::G1, Square::onFileRank(File::FG, Rank::R1));
	assertEquals(Square::H1, Square::onFileRank(File::FH, Rank::R1));

	assertEquals(Square::A2, Square::onFileRank(File::FA, Rank::R2));
	assertEquals(Square::B2, Square::onFileRank(File::FB, Rank::R2));
	assertEquals(Square::C2, Square::onFileRank(File::FC, Rank::R2));
	assertEquals(Square::D2, Square::onFileRank(File::FD, Rank::R2));
	assertEquals(Square::E2, Square::onFileRank(File::FE, Rank::R2));
	assertEquals(Square::F2, Square::onFileRank(File::FF, Rank::R2));
	assertEquals(Square::G2, Square::onFileRank(File::FG, Rank::R2));
	assertEquals(Square::H2, Square::onFileRank(File::FH, Rank::R2));

	assertEquals(Square::A3, Square::onFileRank(File::FA, Rank::R3));
	assertEquals(Square::B3, Square::onFileRank(File::FB, Rank::R3));
	assertEquals(Square::C3, Square::onFileRank(File::FC, Rank::R3));
	assertEquals(Square::D3, Square::onFileRank(File::FD, Rank::R3));
	assertEquals(Square::E3, Square::onFileRank(File::FE, Rank::R3));
	assertEquals(Square::F3, Square::onFileRank(File::FF, Rank::R3));
	assertEquals(Square::G3, Square::onFileRank(File::FG, Rank::R3));
	assertEquals(Square::H3, Square::onFileRank(File::FH, Rank::R3));

	assertEquals(Square::A4, Square::onFileRank(File::FA, Rank::R4));
	assertEquals(Square::B4, Square::onFileRank(File::FB, Rank::R4));
	assertEquals(Square::C4, Square::onFileRank(File::FC, Rank::R4));
	assertEquals(Square::D4, Square::onFileRank(File::FD, Rank::R4));
	assertEquals(Square::E4, Square::onFileRank(File::FE, Rank::R4));
	assertEquals(Square::F4, Square::onFileRank(File::FF, Rank::R4));
	assertEquals(Square::G4, Square::onFileRank(File::FG, Rank::R4));
	assertEquals(Square::H4, Square::onFileRank(File::FH, Rank::R4));

	assertEquals(Square::A5, Square::onFileRank(File::FA, Rank::R5));
	assertEquals(Square::B5, Square::onFileRank(File::FB, Rank::R5));
	assertEquals(Square::C5, Square::onFileRank(File::FC, Rank::R5));
	assertEquals(Square::D5, Square::onFileRank(File::FD, Rank::R5));
	assertEquals(Square::E5, Square::onFileRank(File::FE, Rank::R5));
	assertEquals(Square::F5, Square::onFileRank(File::FF, Rank::R5));
	assertEquals(Square::G5, Square::onFileRank(File::FG, Rank::R5));
	assertEquals(Square::H5, Square::onFileRank(File::FH, Rank::R5));

	assertEquals(Square::A6, Square::onFileRank(File::FA, Rank::R6));
	assertEquals(Square::B6, Square::onFileRank(File::FB, Rank::R6));
	assertEquals(Square::C6, Square::onFileRank(File::FC, Rank::R6));
	assertEquals(Square::D6, Square::onFileRank(File::FD, Rank::R6));
	assertEquals(Square::E6, Square::onFileRank(File::FE, Rank::R6));
	assertEquals(Square::F6, Square::onFileRank(File::FF, Rank::R6));
	assertEquals(Square::G6, Square::onFileRank(File::FG, Rank::R6));
	assertEquals(Square::H6, Square::onFileRank(File::FH, Rank::R6));

	assertEquals(Square::A7, Square::onFileRank(File::FA, Rank::R7));
	assertEquals(Square::B7, Square::onFileRank(File::FB, Rank::R7));
	assertEquals(Square::C7, Square::onFileRank(File::FC, Rank::R7));
	assertEquals(Square::D7, Square::onFileRank(File::FD, Rank::R7));
	assertEquals(Square::E7, Square::onFileRank(File::FE, Rank::R7));
	assertEquals(Square::F7, Square::onFileRank(File::FF, Rank::R7));
	assertEquals(Square::G7, Square::onFileRank(File::FG, Rank::R7));
	assertEquals(Square::H7, Square::onFileRank(File::FH, Rank::R7));

	assertEquals(Square::A8, Square::onFileRank(File::FA, Rank::R8));
	assertEquals(Square::B8, Square::onFileRank(File::FB, Rank::R8));
	assertEquals(Square::C8, Square::onFileRank(File::FC, Rank::R8));
	assertEquals(Square::D8, Square::onFileRank(File::FD, Rank::R8));
	assertEquals(Square::E8, Square::onFileRank(File::FE, Rank::R8));
	assertEquals(Square::F8, Square::onFileRank(File::FF, Rank::R8));
	assertEquals(Square::G8, Square::onFileRank(File::FG, Rank::R8));
	assertEquals(Square::H8, Square::onFileRank(File::FH, Rank::R8));
}

TEST (testGetFileGetRank) {
	for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
		const File::Type file = Square::getFile(square);
		const Rank::Type rank = Square::getRank(square);

		assertEquals(square, Square::onFileRank(file, rank));
	}
}

TEST (testGetOppositeSquare) {
	for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
		const Square::Type oppositeSquare = Square::getOppositeSquare(square);

		assertEquals(Square::getFile(square), Square::getFile(oppositeSquare));
		assertEquals(Rank::getOppositeRank(Square::getRank(square)), Square::getRank(oppositeSquare));
	}
}

TEST_RUNNER_BEGIN
	RUN_TEST(testOnFileRank);
	RUN_TEST(testGetFileGetRank);
	RUN_TEST(testGetOppositeSquare);
TEST_RUNNER_END


