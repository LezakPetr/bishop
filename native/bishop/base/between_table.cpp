

#include "between_table.h"
#include "file_rank_offset.h"
#include "figure_move_offsets.h"


using namespace bishop::base;


const Table<BetweenTable::SIZE, BitBoard::Type, BetweenTable::SQUARE1_SHIFT, BetweenTable::SQUARE2_SHIFT> bishop::base::BetweenTable::getItem([](auto &table) {
	const PieceType::Type figure = PieceType::QUEEN;
	const int directionCount = FigureMoveOffsets::getFigureDirectionCount (figure);

	for (Square::Type beginSquare = Square::FIRST; beginSquare < Square::LAST; beginSquare++) {
		for (int direction = 0; direction < directionCount; direction++) {
			const FileRankOffset offset = FigureMoveOffsets::getFigureOffset (figure, direction);
			BitBoard::Type mask = BitBoard::EMPTY;

			File::Type file = Square::getFile(beginSquare) + offset.fileOffset;
			Rank::Type rank = Square::getRank(beginSquare) + offset.rankOffset;

			while (File::isValid(file) && Rank::isValid(rank)) {
				const Square::Type targetSquare = Square::onFileRank(file, rank);
				table(beginSquare, targetSquare) = mask;

				mask |= BitBoard::getSquareMask(targetSquare);

				file += offset.fileOffset;
				rank += offset.rankOffset;
			}
		}
	}
});

