
#include "pawn_move_table.h"
#include "board_constants.h"
#include "../../util/direct_table_initializer.h"


using namespace bishop::base;


const Table<PawnMoveTable::SIZE, BitBoard::Type, PawnMoveTable::COLOR_SHIFT, PawnMoveTable::SQUARE_SHIFT> bishop::base::PawnMoveTable::getItem(
	makeDirectTableInitializer([](Color::Type color, Square::Type square) {
		const File::Type file = Square::getFile(square);
		const Rank::Type rank = Square::getRank(square);
	
		BitBoard::Type board = BitBoard::EMPTY;
	
		if (rank >= Rank::R2 && rank <= Rank::R7) {
			const Rank::Difference rankOffset = BoardConstants::getPawnRankOffset(color);

			board |= BitBoard::getSquareMask(Square::onFileRank(file, rank + rankOffset));

			if ((color == Color::WHITE && rank == Rank::R2) || (color == Color::BLACK && rank == Rank::R7))
				board |= BitBoard::getSquareMask(Square::onFileRank(file, rank + 2*rankOffset));
		}

		return board;
	}).withBounds<Color::FIRST, Color::LAST, Square::FIRST, Square::LAST>()
);

