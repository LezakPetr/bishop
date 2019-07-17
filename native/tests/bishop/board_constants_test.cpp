#include "../../bishop/base/color.h"
#include "../../bishop/base/board_constants.h"
#include "../../bishop/base/figure_attack_table.h"
#include "../test_base/test_base.h"

#include <random>
#include <algorithm>
#include <string>

using namespace bishop::base;
using namespace std;


TEST (getSquareColorMaskTest) {
	assertEquals(BoardConstants::WHITE_SQUARE_MASK, BoardConstants::getSquareColorMask(Color::WHITE));
	assertEquals(BoardConstants::BLACK_SQUARE_MASK, BoardConstants::getSquareColorMask(Color::BLACK));
}

TEST (getRankMaskTest) {
	assertEquals(BoardConstants::RANK_1_MASK, BoardConstants::getRankMask(Rank::R1));
	assertEquals(BoardConstants::RANK_2_MASK, BoardConstants::getRankMask(Rank::R2));
	assertEquals(BoardConstants::RANK_3_MASK, BoardConstants::getRankMask(Rank::R3));
	assertEquals(BoardConstants::RANK_4_MASK, BoardConstants::getRankMask(Rank::R4));
	assertEquals(BoardConstants::RANK_5_MASK, BoardConstants::getRankMask(Rank::R5));
	assertEquals(BoardConstants::RANK_6_MASK, BoardConstants::getRankMask(Rank::R6));
	assertEquals(BoardConstants::RANK_7_MASK, BoardConstants::getRankMask(Rank::R7));
	assertEquals(BoardConstants::RANK_8_MASK, BoardConstants::getRankMask(Rank::R8));
}

TEST (getFileMaskTest) {
	assertEquals(BoardConstants::FILE_A_MASK, BoardConstants::getFileMask(File::FA));
	assertEquals(BoardConstants::FILE_B_MASK, BoardConstants::getFileMask(File::FB));
	assertEquals(BoardConstants::FILE_C_MASK, BoardConstants::getFileMask(File::FC));
	assertEquals(BoardConstants::FILE_D_MASK, BoardConstants::getFileMask(File::FD));
	assertEquals(BoardConstants::FILE_E_MASK, BoardConstants::getFileMask(File::FE));
	assertEquals(BoardConstants::FILE_F_MASK, BoardConstants::getFileMask(File::FF));
	assertEquals(BoardConstants::FILE_G_MASK, BoardConstants::getFileMask(File::FG));
	assertEquals(BoardConstants::FILE_H_MASK, BoardConstants::getFileMask(File::FH));
}

TEST (getEpRankTest) {
	assertEquals(Rank::R4, BoardConstants::getEpRank(Color::WHITE));
	assertEquals(Rank::R5, BoardConstants::getEpRank(Color::BLACK));
}

TEST (getEpSquareTest) {
	for (File::Type file = File::FIRST; file < File::LAST; file++) {
		assertEquals(Square::onFileRank(file, Rank::R4), BoardConstants::getEpSquare(Color::WHITE, file));
		assertEquals(Square::onFileRank(file, Rank::R5), BoardConstants::getEpSquare(Color::BLACK, file));
	}
}

TEST (getEpTargetSquareTest) {
	for (File::Type file = File::FIRST; file < File::LAST; file++) {
		assertEquals(Square::onFileRank(file, Rank::R3), BoardConstants::getEpTargetSquare(Color::WHITE, file));
		assertEquals(Square::onFileRank(file, Rank::R6), BoardConstants::getEpTargetSquare(Color::BLACK, file));
	}
}

TEST (getEpRankMaskTest) {
	assertEquals(BoardConstants::RANK_4_MASK, BoardConstants::getEpRankMask(Color::WHITE));
	assertEquals(BoardConstants::RANK_5_MASK, BoardConstants::getEpRankMask(Color::BLACK));
}

TEST (getPawnInitialSquare) {
	assertEquals(Square::A2, BoardConstants::getPawnInitialSquare(Color::WHITE, File::FA));
	assertEquals(Square::B2, BoardConstants::getPawnInitialSquare(Color::WHITE, File::FB));
	assertEquals(Square::C2, BoardConstants::getPawnInitialSquare(Color::WHITE, File::FC));
	assertEquals(Square::D2, BoardConstants::getPawnInitialSquare(Color::WHITE, File::FD));
	assertEquals(Square::E2, BoardConstants::getPawnInitialSquare(Color::WHITE, File::FE));
	assertEquals(Square::F2, BoardConstants::getPawnInitialSquare(Color::WHITE, File::FF));
	assertEquals(Square::G2, BoardConstants::getPawnInitialSquare(Color::WHITE, File::FG));
	assertEquals(Square::H2, BoardConstants::getPawnInitialSquare(Color::WHITE, File::FH));

	assertEquals(Square::A7, BoardConstants::getPawnInitialSquare(Color::BLACK, File::FA));
	assertEquals(Square::B7, BoardConstants::getPawnInitialSquare(Color::BLACK, File::FB));
	assertEquals(Square::C7, BoardConstants::getPawnInitialSquare(Color::BLACK, File::FC));
	assertEquals(Square::D7, BoardConstants::getPawnInitialSquare(Color::BLACK, File::FD));
	assertEquals(Square::E7, BoardConstants::getPawnInitialSquare(Color::BLACK, File::FE));
	assertEquals(Square::F7, BoardConstants::getPawnInitialSquare(Color::BLACK, File::FF));
	assertEquals(Square::G7, BoardConstants::getPawnInitialSquare(Color::BLACK, File::FG));
	assertEquals(Square::H7, BoardConstants::getPawnInitialSquare(Color::BLACK, File::FH));
}

TEST (getKingSquareDistanceTest) {
	for (Rank::Type rank1 = Rank::FIRST; rank1 < Rank::LAST; rank1++) {
		for (File::Type file1 = File::FIRST; file1 < File::LAST; file1++) {
			for (Rank::Type rank2 = Rank::FIRST; rank2 < Rank::LAST; rank2++) {
				for (File::Type file2 = File::FIRST; file2 < File::LAST; file2++) {
					const int dFile = abs(file2 - file1);
					const int dRank = abs(rank2 - rank1);
					Square::Difference distance = max(dFile, dRank);

					assertEquals(
						distance,
						BoardConstants::getKingSquareDistance(
							Square::onFileRank(file1, rank1),
							Square::onFileRank(file2, rank2)
						)
					);
				}
			}
		}
	}
}

TEST (getPawnPromotionRank) {
	assertEquals(Rank::R8, BoardConstants::getPawnPromotionRank(Color::WHITE));
	assertEquals(Rank::R1, BoardConstants::getPawnPromotionRank(Color::BLACK));
}

TEST (getPawnPromotionSquare) {
	for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
		const File::Type file = Square::getFile(square);
		const Rank::Type rank = Square::getRank(square);

		assertEquals(
			Square::onFileRank(file, Rank::R8),
			BoardConstants::getPawnPromotionSquare(
				Color::WHITE,
				Square::onFileRank(file, rank)
			)
		);

		assertEquals(
			Square::onFileRank(file, Rank::R1),
			BoardConstants::getPawnPromotionSquare(
				Color::BLACK,
				Square::onFileRank(file, rank)
			)
		);
	}
}

TEST (getPawnPromotionDistanceTest) {
	for (int file = File::FIRST; file < File::LAST; file++) {
		assertEquals(0, BoardConstants::getPawnPromotionDistance(Color::WHITE, Square::onFileRank(file, Rank::R8)));
		assertEquals(1, BoardConstants::getPawnPromotionDistance(Color::WHITE, Square::onFileRank(file, Rank::R7)));
		assertEquals(2, BoardConstants::getPawnPromotionDistance(Color::WHITE, Square::onFileRank(file, Rank::R6)));
		assertEquals(3, BoardConstants::getPawnPromotionDistance(Color::WHITE, Square::onFileRank(file, Rank::R5)));
		assertEquals(4, BoardConstants::getPawnPromotionDistance(Color::WHITE, Square::onFileRank(file, Rank::R4)));
		assertEquals(5, BoardConstants::getPawnPromotionDistance(Color::WHITE, Square::onFileRank(file, Rank::R3)));
		assertEquals(5, BoardConstants::getPawnPromotionDistance(Color::WHITE, Square::onFileRank(file, Rank::R2)));

		assertEquals(0, BoardConstants::getPawnPromotionDistance(Color::BLACK, Square::onFileRank(file, Rank::R1)));
		assertEquals(1, BoardConstants::getPawnPromotionDistance(Color::BLACK, Square::onFileRank(file, Rank::R2)));
		assertEquals(2, BoardConstants::getPawnPromotionDistance(Color::BLACK, Square::onFileRank(file, Rank::R3)));
		assertEquals(3, BoardConstants::getPawnPromotionDistance(Color::BLACK, Square::onFileRank(file, Rank::R4)));
		assertEquals(4, BoardConstants::getPawnPromotionDistance(Color::BLACK, Square::onFileRank(file, Rank::R5)));
		assertEquals(5, BoardConstants::getPawnPromotionDistance(Color::BLACK, Square::onFileRank(file, Rank::R6)));
		assertEquals(5, BoardConstants::getPawnPromotionDistance(Color::BLACK, Square::onFileRank(file, Rank::R7)));
	}
}

TEST (getPawnRankOffsetTest) {
	assertEquals((Rank::Difference) +1, BoardConstants::getPawnRankOffset(Color::WHITE));
	assertEquals((Rank::Difference) -1, BoardConstants::getPawnRankOffset(Color::BLACK));
}

TEST (getPawnSquareOffsetTest) {
	assertEquals((Square::Difference) +File::COUNT, BoardConstants::getPawnSquareOffset(Color::WHITE));
	assertEquals((Square::Difference) -File::COUNT, BoardConstants::getPawnSquareOffset(Color::BLACK));
}

static void testGetFrontSquaresOnThreeFiles(const Color::Type color, const Square::Type square, const string &expectedBoard) {
	assertEquals(
		BitBoard::fromString(expectedBoard),
		BoardConstants::getFrontSquaresOnThreeFiles(color, square)
	);
}

TEST (getFrontSquaresOnThreeFilesTest) {
	testGetFrontSquaresOnThreeFiles(Color::WHITE, Square::A4, "a5, a6, a7, a8, b5, b6, b7, b8");
	testGetFrontSquaresOnThreeFiles(Color::WHITE, Square::H5, "g6, g7, g8, h6, h7, h8");
	testGetFrontSquaresOnThreeFiles(Color::WHITE, Square::C6, "b7, b8, c7, c8, d7, d8");
	testGetFrontSquaresOnThreeFiles(Color::BLACK, Square::A4, "a3, a2, a1, b3, b2, b1");
	testGetFrontSquaresOnThreeFiles(Color::BLACK, Square::H5, "g4, g3, g2, g1, h4, h3, h2, h1");
	testGetFrontSquaresOnThreeFiles(Color::BLACK, Square::E2, "d1, e1, f1");
}

static void testGetFrontSquaresOnNeighborFiles(const Color::Type color, const Square::Type square, const string &expectedBoard) {
	assertEquals(
		BitBoard::fromString(expectedBoard),
		BoardConstants::getFrontSquaresOnNeighborFiles(color, square)
	);
}

TEST (getFrontSquaresOnNeighborFilesTest) {
	testGetFrontSquaresOnNeighborFiles(Color::WHITE, Square::A4, "b5, b6, b7, b8");
	testGetFrontSquaresOnNeighborFiles(Color::WHITE, Square::H5, "g6, g7, g8");
	testGetFrontSquaresOnNeighborFiles(Color::WHITE, Square::C6, "b7, b8, d7, d8");
	testGetFrontSquaresOnNeighborFiles(Color::BLACK, Square::A4, "b3, b2, b1");
	testGetFrontSquaresOnNeighborFiles(Color::BLACK, Square::H5, "g4, g3, g2, g1");
	testGetFrontSquaresOnNeighborFiles(Color::BLACK, Square::E2, "d1, f1");
}

static void testGetPawnBlockingSquares(const Color::Type color, const Square::Type square, const string &expectedBoard) {
	assertEquals(
		BitBoard::fromString(expectedBoard),
		BoardConstants::getPawnBlockingSquares(color, square)
	);
}

TEST (getPawnBlockingSquaresTest) {
	testGetPawnBlockingSquares(Color::WHITE, Square::A4, "a5, a6, a7, a8, b6, b7, b8");
	testGetPawnBlockingSquares(Color::WHITE, Square::H5, "g7, g8, h6, h7, h8");
	testGetPawnBlockingSquares(Color::WHITE, Square::C6, "b8, c7, c8, d8");
	testGetPawnBlockingSquares(Color::BLACK, Square::A4, "a3, a2, a1, b2, b1");
	testGetPawnBlockingSquares(Color::BLACK, Square::H5, "g3, g2, g1, h4, h3, h2, h1");
	testGetPawnBlockingSquares(Color::BLACK, Square::E2, "e1");
}

static void testGetSquaresInFrontInclusive(const Color::Type color, const Square::Type square, const string &expectedBoard) {
	assertEquals(
		BitBoard::fromString(expectedBoard),
		BoardConstants::getSquaresInFrontInclusive(color, square)
	);
}

TEST (getSquaresInFrontInclusiveTest) {
	testGetSquaresInFrontInclusive(Color::WHITE, Square::A4, "a4, a5, a6, a7, a8");
	testGetSquaresInFrontInclusive(Color::WHITE, Square::H5, "h5, h6, h7, h8");
	testGetSquaresInFrontInclusive(Color::WHITE, Square::C6, "c6, c7, c8");
	testGetSquaresInFrontInclusive(Color::BLACK, Square::A4, "a4, a3, a2, a1");
	testGetSquaresInFrontInclusive(Color::BLACK, Square::H5, "h5, h4, h3, h2, h1");
	testGetSquaresInFrontInclusive(Color::BLACK, Square::E2, "e2, e1");
}

static void testGetSquaresInFrontExclusive(const Color::Type color, const Square::Type square, const string &expectedBoard) {
	assertEquals(
			BitBoard::fromString(expectedBoard),
			BoardConstants::getSquaresInFrontExclusive(color, square)
	);
}

TEST (getSquaresInFrontExclusiveTest) {
	testGetSquaresInFrontExclusive(Color::WHITE, Square::A4, "a5, a6, a7, a8");
	testGetSquaresInFrontExclusive(Color::WHITE, Square::H5, "h6, h7, h8");
	testGetSquaresInFrontExclusive(Color::WHITE, Square::C6, "c7, c8");
	testGetSquaresInFrontExclusive(Color::BLACK, Square::A4, "a3, a2, a1");
	testGetSquaresInFrontExclusive(Color::BLACK, Square::H5, "h4, h3, h2, h1");
	testGetSquaresInFrontExclusive(Color::BLACK, Square::E2, "e1");
}

TEST (getConnectedPawnSquareMaskTest) {
	for (File::Type file = File::FIRST; file < File::LAST; file++) {
		for (Rank::Type rank = Rank::FIRST; rank < Rank::LAST; rank++) {
			const Square::Type square = Square::onFileRank(file, rank);

			BitBoard::Type expectedMask = BitBoard::EMPTY;

			if (file > File::FA)
				expectedMask |= BitBoard::of(Square::onFileRank(file - 1, rank));

			if (file < File::FH)
				expectedMask |= BitBoard::of(Square::onFileRank(file + 1, rank));

			assertEquals(expectedMask, BoardConstants::getConnectedPawnSquareMask(square));
		}
	}
}

TEST (getAllConnectedPawnSquareMaskTest) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution;

	for (int i = 0; i < 100000; i++) {
		BitBoard::Type sourceMask = BitBoard::EMPTY;
		BitBoard::Type expectedMask = BitBoard::EMPTY;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			if (squareDistribution(rng)) {
				sourceMask |= BitBoard::of(square);
				expectedMask |= BoardConstants::getConnectedPawnSquareMask(square);
			}
		}

		assertEquals(expectedMask, BoardConstants::getAllConnectedPawnSquareMask(sourceMask));
	}
}

TEST (getFirstRankMaskTest) {
	assertEquals(BoardConstants::RANK_1_MASK, BoardConstants::getFirstRankMask(Color::WHITE));
	assertEquals(BoardConstants::RANK_8_MASK, BoardConstants::getFirstRankMask(Color::BLACK));
}

TEST (getSecondRankMaskTest) {
	assertEquals(BoardConstants::RANK_2_MASK, BoardConstants::getSecondRankMask(Color::WHITE));
	assertEquals(BoardConstants::RANK_7_MASK, BoardConstants::getSecondRankMask(Color::BLACK));
}

TEST (getPawnsAttackedSquaresFromLeftTest) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution(0.1);

	for (int i = 0; i < 100000; i++) {
		BitBoard::Type sourceMask = BitBoard::EMPTY;
		BitBoard::Type expectedWhiteMask = BitBoard::EMPTY;
		BitBoard::Type expectedBlackMask = BitBoard::EMPTY;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			const File::Type file = Square::getFile(square);
			const Rank::Type rank = Square::getRank(square);

			if (squareDistribution(rng)) {
				sourceMask |= BitBoard::of(square);

				if (file < File::FH && rank < Rank::R8)
					expectedWhiteMask |= BitBoard::of(Square::onFileRank(file + 1, rank + 1));

				if (file < File::FH && rank > Rank::R1)
					expectedBlackMask |= BitBoard::of(Square::onFileRank(file + 1, rank - 1));
			}
		}

		assertEquals(expectedWhiteMask, BoardConstants::getPawnsAttackedSquaresFromLeft(Color::WHITE, sourceMask));
		assertEquals(expectedBlackMask, BoardConstants::getPawnsAttackedSquaresFromLeft(Color::BLACK, sourceMask));
	}
}

TEST (getPawnsAttackedSquaresFromRightTest) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution(0.1);

	for (int i = 0; i < 100000; i++) {
		BitBoard::Type sourceMask = BitBoard::EMPTY;
		BitBoard::Type expectedWhiteMask = BitBoard::EMPTY;
		BitBoard::Type expectedBlackMask = BitBoard::EMPTY;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			const File::Type file = Square::getFile(square);
			const Rank::Type rank = Square::getRank(square);

			if (squareDistribution(rng)) {
				sourceMask |= BitBoard::of(square);

				if (file > File::FA && rank < Rank::R8)
					expectedWhiteMask |= BitBoard::of(Square::onFileRank(file - 1, rank + 1));

				if (file > File::FA && rank > Rank::R1)
					expectedBlackMask |= BitBoard::of(Square::onFileRank(file - 1, rank - 1));
			}
		}

		assertEquals(expectedWhiteMask, BoardConstants::getPawnsAttackedSquaresFromRight(Color::WHITE, sourceMask));
		assertEquals(expectedBlackMask, BoardConstants::getPawnsAttackedSquaresFromRight(Color::BLACK, sourceMask));
	}
}

TEST (getPawnsAttackedSquaresTest) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution(0.1);

	for (int i = 0; i < 100000; i++) {
		BitBoard::Type sourceMask = BitBoard::EMPTY;
		BitBoard::Type expectedWhiteMask = BitBoard::EMPTY;
		BitBoard::Type expectedBlackMask = BitBoard::EMPTY;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			const File::Type file = Square::getFile(square);
			const Rank::Type rank = Square::getRank(square);

			if (squareDistribution(rng)) {
				sourceMask |= BitBoard::of(square);
				
				if (file < File::FH && rank < Rank::R8)
					expectedWhiteMask |= BitBoard::of(Square::onFileRank(file + 1, rank + 1));

				if (file < File::FH && rank > Rank::R1)
					expectedBlackMask |= BitBoard::of(Square::onFileRank(file + 1, rank - 1));

				if (file > File::FA && rank < Rank::R8)
					expectedWhiteMask |= BitBoard::of(Square::onFileRank(file - 1, rank + 1));

				if (file > File::FA && rank > Rank::R1)
					expectedBlackMask |= BitBoard::of(Square::onFileRank(file - 1, rank - 1));
			}
		}

		assertEquals(expectedWhiteMask, BoardConstants::getPawnsAttackedSquares(Color::WHITE, sourceMask));
		assertEquals(expectedBlackMask, BoardConstants::getPawnsAttackedSquares(Color::BLACK, sourceMask));
	}
}

TEST (getKingsAttackedSquaresTest) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution(0.1);

	for (int i = 0; i < 100000; i++) {
		BitBoard::Type sourceMask = BitBoard::EMPTY;
		BitBoard::Type expectedMask = BitBoard::EMPTY;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			if (squareDistribution(rng)) {
				sourceMask |= BitBoard::of(square);
				expectedMask |= FigureAttackTable::getItem(PieceType::KING, square);
			}
		}

		assertEquals(expectedMask, BoardConstants::getKingsAttackedSquares(sourceMask));
	}
}

TEST (getPawnSingleMoveSquaresTest) {
	::std::minstd_rand rng;
	::std::bernoulli_distribution squareDistribution(0.1);
	::std::uniform_int_distribution<Color::Type> colorDistribution (Color::FIRST, Color::LAST - 1);

	for (int i = 0; i < 100000; i++) {
		const Color::Type color = colorDistribution(rng);

		BitBoard::Type sourceMask = BitBoard::EMPTY;
		BitBoard::Type expectedMask = BitBoard::EMPTY;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			if (squareDistribution(rng)) {
				sourceMask |= BitBoard::of(square);

				const Square::Type targetSquare = square + File::COUNT * BoardConstants::getPawnRankOffset(color);

				if (Square::isValid(targetSquare))
					expectedMask |= BitBoard::of(targetSquare);
			}
		}

		assertEquals(expectedMask, BoardConstants::getPawnSingleMoveSquares(color, sourceMask));
	}
}

TEST (getPrevEpFileMaskTest) {
	for (Color::Type color = Color::FIRST; color < Color::LAST; color++) {
		for (File::Type file = File::FIRST; file < File::LAST; file++) {
			const BitBoard::Type epSquareMask = BoardConstants::getPrevEpFileMask(color, file);

			if (file == File::FA) {
				assertEquals(BitBoard::EMPTY, epSquareMask);
			}
		       	else {
				assertEquals(1, BitBoard::getSquareCount(epSquareMask));

				const Square::Type epSquare = BitBoard::getFirstSquare(epSquareMask);
				assertEquals((File::Type) (file - 1), Square::getFile(epSquare));
				assertEquals(BoardConstants::getEpRank(color), Square::getRank(epSquare));
			}
		}
	}
}

TEST (getNextEpFileMaskTest) {
	for (Color::Type color = Color::FIRST; color < Color::LAST; color++) {
		for (File::Type file = File::FIRST; file < File::LAST; file++) {
			const BitBoard::Type epSquareMask = BoardConstants::getNextEpFileMask(color, file);

			if (file == File::FH) {
				assertEquals(BitBoard::EMPTY, epSquareMask);
			}
		       	else {
				assertEquals(1, BitBoard::getSquareCount(epSquareMask));

				const Square::Type epSquare = BitBoard::getFirstSquare(epSquareMask);
				assertEquals((File::Type) (file + 1), Square::getFile(epSquare));
				assertEquals(BoardConstants::getEpRank(color), Square::getRank(epSquare));
			}
		}
	}
}

TEST (getPieceAllowedSquaresTest) {
	assertEquals(BoardConstants::PAWN_ALLOWED_SQUARES, BoardConstants::getPieceAllowedSquares(PieceType::PAWN));

	for (PieceType::Type pieceType = PieceType::FIGURE_FIRST; pieceType < PieceType::FIGURE_LAST; pieceType++)
		assertEquals(BitBoard::FULL, BoardConstants::getPieceAllowedSquares(pieceType));
}

TEST (getMinFileDistanceTest) {
	assertEquals(File::LAST, BoardConstants::getMinFileDistance(0, File::FB));

	const int fileMask = (1 << File::FB) | (1 << File::FC) | (1 << File::FD) | (1 << File::FH);
	assertEquals((File::Difference) 2, BoardConstants::getMinFileDistance(fileMask, File::FA));
	assertEquals((File::Difference) 1, BoardConstants::getMinFileDistance(fileMask, File::FB));
	assertEquals((File::Difference) 0, BoardConstants::getMinFileDistance(fileMask, File::FC));
	assertEquals((File::Difference) 1, BoardConstants::getMinFileDistance(fileMask, File::FD));
	assertEquals((File::Difference) 2, BoardConstants::getMinFileDistance(fileMask, File::FE));
	assertEquals((File::Difference) 2, BoardConstants::getMinFileDistance(fileMask, File::FF));
	assertEquals((File::Difference) 1, BoardConstants::getMinFileDistance(fileMask, File::FG));
	assertEquals((File::Difference) 0, BoardConstants::getMinFileDistance(fileMask, File::FH));
}

TEST (getKingNearSquaresTest) {
	for (Square::Type kingSquare = Square::FIRST; kingSquare < Square::LAST; kingSquare++) {
		BitBoard::Type expectedMask = BitBoard::EMPTY;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			if (BoardConstants::getKingSquareDistance(kingSquare, square) <= 1)
				expectedMask |= BitBoard::of(square);
		}

		assertEquals(expectedMask, BoardConstants::getKingNearSquares(kingSquare));
	}
}

TEST (getKingSafetyFarSquaresTest) {
	assertEquals(
			BitBoard::fromString("f8, g8, h8, f7, g7, h7, f6, g6, h6"),
			BoardConstants::getKingSafetyFarSquares(Square::G8)
	);

	assertEquals(
			BitBoard::fromString("a1, b1, a2, b2, a3, b3"),
			BoardConstants::getKingSafetyFarSquares(Square::A1)
	);
}

TEST_RUNNER_BEGIN
	RUN_TEST (getSquareColorMaskTest);
	RUN_TEST (getRankMaskTest);
	RUN_TEST (getFileMaskTest);
	RUN_TEST (getEpRankTest);
	RUN_TEST (getEpSquareTest);
	RUN_TEST (getEpTargetSquareTest);
	RUN_TEST (getEpRankMaskTest);
	RUN_TEST (getPawnInitialSquare);
	RUN_TEST (getKingSquareDistanceTest);
	RUN_TEST (getPawnPromotionRank);
	RUN_TEST (getPawnPromotionSquare);
	RUN_TEST (getPawnPromotionDistanceTest);
	RUN_TEST (getPawnRankOffsetTest);
	RUN_TEST (getPawnSquareOffsetTest);
	RUN_TEST (getFrontSquaresOnThreeFilesTest);
	RUN_TEST (getFrontSquaresOnNeighborFilesTest);
	RUN_TEST (getPawnBlockingSquaresTest);
	RUN_TEST (getSquaresInFrontInclusiveTest);
	RUN_TEST (getSquaresInFrontExclusiveTest);
	RUN_TEST (getConnectedPawnSquareMaskTest);
	RUN_TEST (getAllConnectedPawnSquareMaskTest);
	RUN_TEST (getFirstRankMaskTest);
	RUN_TEST (getSecondRankMaskTest);
	RUN_TEST (getPawnsAttackedSquaresFromLeftTest);
	RUN_TEST (getPawnsAttackedSquaresFromRightTest);
	RUN_TEST (getPawnsAttackedSquaresTest);
	RUN_TEST (getKingsAttackedSquaresTest);
	RUN_TEST (getPawnSingleMoveSquaresTest);
	RUN_TEST (getPrevEpFileMaskTest);
	RUN_TEST (getNextEpFileMaskTest);
	RUN_TEST (getPieceAllowedSquaresTest);
	RUN_TEST (getMinFileDistanceTest);
	RUN_TEST (getKingNearSquaresTest);
	RUN_TEST (getKingSafetyFarSquaresTest);
TEST_RUNNER_END

