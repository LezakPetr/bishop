
#include "bit_board.h"
#include "../../util/table.h"
#include "../../util/direct_table_initializer.h"
#include <cstdio>

using namespace bishop::base;
using namespace std;


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

BitBoard::Type bishop::base::BitBoard::fromString (::std::string const & str) {
	BitBoard::Type mask = BitBoard::EMPTY;
	size_t pos = 0;

	while (true) {
		const size_t next = str.find (',', pos);
		const string token = (next == string::npos) ? str.substr (pos) : str.substr (pos, next - pos);

		mask |= BitBoard::of (Square::fromString (token));

		if (next == string::npos)
			break;

		pos = next + 1;
	}

	return mask;
}

