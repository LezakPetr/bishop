
#include "board_constants.h"
#include "../../util/direct_table_initializer.h"
#include "figure_attack_table.h"

#include <cstdlib>
#include <algorithm>


using namespace bishop::base;
using namespace std;


const int bishop::base::BoardConstants::PAWN_PROMOTION_DISTANCES[Color::LAST][Rank::LAST] = {
	{
		-1, 5, 5, 4, 3, 2, 1, 0 // White
	},
	{
		0, 1, 2, 3, 4, 5, 5, -1 // Black
	}
};

const ColoredDoubleSquareTable bishop::base::BoardConstants::getFrontSquaresOnThreeFiles(
	makeColoredDoubleSquareTableInitializer(
		[](const Square::Type d, const Square::Type m) {
			return Square::getRank(m) > Square::getRank(d) && abs(Square::getFile(m) - Square::getFile(d)) <= 1;	
		}
	)
);

const ColoredDoubleSquareTable bishop::base::BoardConstants::getFrontSquaresOnNeighborFiles(
	makeColoredDoubleSquareTableInitializer(
		[](const Square::Type d, const Square::Type m) {
			return Square::getRank(m) > Square::getRank(d) && abs(Square::getFile(m) - Square::getFile(d)) == 1;
		}
	)
);

const ColoredDoubleSquareTable bishop::base::BoardConstants::getPawnBlockingSquares(
	makeColoredDoubleSquareTableInitializer(
		[](const Square::Type d, const Square::Type m) {
			const Rank::Type dRank = Square::getRank(d);
			const Rank::Type mRank = Square::getRank(m);
			const File::Type dFile = Square::getFile(d);
			const File::Type mFile = Square::getFile(m);

			return (dFile == mFile && mRank > dRank) || (abs(dFile - mFile) == 1 && mRank - dRank >= 2);
		}
	)
);

const ColoredDoubleSquareTable bishop::base::BoardConstants::getSquaresInFrontInclusive(
	makeColoredDoubleSquareTableInitializer(
		[](const Square::Type d, const Square::Type m) {
			return Square::getFile(d) == Square::getFile(m) && Square::getRank(m) >= Square::getRank(d);
		}
	)
);

const ColoredDoubleSquareTable bishop::base::BoardConstants::getSquaresInFrontExclusive(
	makeColoredDoubleSquareTableInitializer(
		[](const Square::Type d, const Square::Type m) {
			return Square::getFile(d) == Square::getFile(m) && Square::getRank(m) > Square::getRank(d);
		}
	)
);

const DoubleSquareTable bishop::base::BoardConstants::getConnectedPawnSquareMask(
	makeDoubleSquareTableInitializer(
		[](const Square::Type d, const Square::Type m) {
			return Square::getRank(m) == Square::getRank(d) && abs(Square::getFile(m) - Square::getFile(d)) == 1;
		}
	)
);

const DoubleSquareTable bishop::base::BoardConstants::getKingSafetyFarSquares(
	makeDoubleSquareTableInitializer(
		[](const Square::Type d, const Square::Type m) {
			const Rank::Type dRank = Square::getRank(d);
			const Rank::Type mRank = Square::getRank(m);
			const File::Type dFile = Square::getFile(d);
			const File::Type mFile = Square::getFile(m);

			return abs(dFile - mFile) <= 1 && abs(dRank - mRank) <= 2;
		}
	)
);

const Table<Color::COUNT * File::COUNT, BitBoard::Type, File::BIT_COUNT, 0> bishop::base::BoardConstants::getPrevEpFileMask(
	[](auto &table) {
		for (Color::Type color = Color::FIRST; color < Color::LAST; color++) {
			const Rank::Type rank = getEpRank(color);

			for (File::Type file = File::FIRST; file < File::LAST; file++) {
				BitBoard::Type board = BitBoard::EMPTY;

				if (file > File::FA) {
					const Square::Type prevSquare = Square::onFileRank(file - 1, rank);
					board |= BitBoard::getSquareMask(prevSquare);
				}

				table(color, file) = board;
			}
		}

	}	
);

const Table<Color::COUNT * File::COUNT, BitBoard::Type, File::BIT_COUNT, 0> bishop::base::BoardConstants::getNextEpFileMask(
	[](auto &table) {
		for (Color::Type color = Color::FIRST; color < Color::LAST; color++) {
			const Rank::Type rank = getEpRank(color);

			for (File::Type file = File::FIRST; file < File::LAST; file++) {
				BitBoard::Type board = BitBoard::EMPTY;

				if (file < File::FH) {
					const Square::Type nextSquare = Square::onFileRank(file + 1, rank);
					board |= BitBoard::getSquareMask(nextSquare);
				}

				table(color, file) = board;
			}
		}

	}	
);

const Table<Square::COUNT * Square::COUNT, Square::Difference, Square::BIT_COUNT, 0> bishop::base::BoardConstants::getKingSquareDistance(
	makeDirectTableInitializer([](const Square::Type beginSquare, const Square::Type endSquare) {
		const File::Type beginFile = Square::getFile(beginSquare);
		const Rank::Type beginRank = Square::getRank(beginSquare);
		const File::Type endFile = Square::getFile(endSquare);
		const Rank::Type endRank = Square::getRank(endSquare);

		return max(abs(beginFile - endFile), abs(beginRank - endRank));
	}).withBounds<Square::FIRST, Square::LAST, Square::FIRST, Square::LAST>()
);

const Table<Square::COUNT, BitBoard::Type, 0> bishop::base::BoardConstants::getKingNearSquares(
	makeDirectTableInitializer([](const Square::Type square) {
		return FigureAttackTable::getItem(PieceType::KING, square) | BitBoard::getSquareMask(square);
	}).withBounds<Square::FIRST, Square::LAST>()
);

const Table<File::COUNT << File::COUNT, File::Difference, File::BIT_COUNT, 0> bishop::base::BoardConstants::getMinFileDistance(
	makeDirectTableInitializer([](const int fileMask, const File::Type file) {
		double minDistance = File::LAST;
		int islandBeginFile = File::NONE;

		// Loop thru all bits of fileMask. We splits the files into islands and calculates the distance
		// to the middle of that islands. The trick with adding one more file ensures that
		// we will always end with bit 0 and finish the last island.
		for (File::Type testedFile = File::FIRST; testedFile <= File::LAST; testedFile++) {
			if ((fileMask & (1 << testedFile)) != 0) {
				if (islandBeginFile == File::NONE)
					islandBeginFile = testedFile;
			}
			else {
				if (islandBeginFile != File::NONE) {
					const File::Type islandEndFile = testedFile - 1;
					const double islandMiddleFile = (islandBeginFile + islandEndFile) / 2.0;
					minDistance = min(minDistance, abs(islandMiddleFile - file));
					islandBeginFile = File::NONE;
				}
			}
		}

		return (u_int8_t) minDistance;
	}).withBounds<0, 1 << File::COUNT, File::FIRST, File::LAST>()
);

