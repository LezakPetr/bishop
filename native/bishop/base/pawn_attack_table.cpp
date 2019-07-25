
#include "pawn_attack_table.h"
#include "board_constants.h"
#include "../../util/direct_table_initializer.h"


using namespace bishop::base;


const Table<PawnAttackTable::SIZE, BitBoard::Type, PawnAttackTable::COLOR_SHIFT, PawnAttackTable::SQUARE_SHIFT> bishop::base::PawnAttackTable::getItem(
	makeDirectTableInitializer([](Color::Type color, Square::Type square) {
		const File::Type file = Square::getFile(square);
		const Rank::Type rank = Square::getRank(square);
		const Rank::Type targetRank = rank + BoardConstants::getPawnRankOffset(color);
	
		BitBoard::Type board = BitBoard::EMPTY;
	
		if (Rank::isValid (targetRank)) {
			if (file >= File::FB)
				board |= BitBoard::getSquareMask(Square::onFileRank(file - 1, targetRank));
	
			if (file <= File::FG)
				board |= BitBoard::getSquareMask(Square::onFileRank(file + 1, targetRank));
		}

		return board;
	}).withBounds<Color::FIRST, Color::LAST, Square::FIRST, Square::LAST>()
);

