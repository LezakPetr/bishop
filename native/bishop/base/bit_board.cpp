
#include "bit_board.h"
#include "../../util/table.h"
#include "../../util/direct_table_initializer.h"
#include <cstdio>

using namespace bishop::base;


Table<BitBoard::NEAR_KING_SQUARE_COUNT_TABLE_SIZE, uint8_t, 0> bishop::base::BitBoard::NEAR_KING_SQUARE_COUNT_TABLE (
	makeDirectTableInitializer([](auto b) { return BitBoard::getSquareCount (b); }).withBounds<0, BitBoard::NEAR_KING_SQUARE_COUNT_TABLE_SIZE>()
);


Table<BitBoard::FIRST_SQUARE_TABLE_SIZE, Square::SmallType, 0> bishop::base::BitBoard::FIRST_SQUARE_TABLE (
	[](auto &table) {
		table.fill (-1);

		table(getFirstSquareTableIndex(BitBoard::EMPTY)) = (Square::SmallType) Square::NONE;

		for (Square::Type square = Square::FIRST; square < Square::LAST; square++) {
			const int index = getFirstSquareTableIndex(BitBoard::of(square));
			assert (table(index) == -1);   // Check for collisions in the table

			table(index) = (Square::SmallType) square;
		}
	}
);

