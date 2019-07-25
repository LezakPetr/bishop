
#ifndef _BISHOP_BASE_BETWEEN_TABLE_H_
#define _BISHOP_BASE_BETWEEN_TABLE_H_


#include "square.h"
#include "bit_board.h"


namespace bishop::base {
	/**
	 * For two squares on same file, rank or diagonal this table contains mask of squares
	 * between given squares (exclusive). For any other squares it contains zeros.
	 * @author Ing. Petr Ležák
	 */
	class BetweenTable {
		private:
			static constexpr int SQUARE2_SHIFT = 0;
			static constexpr int SQUARE1_SHIFT = SQUARE2_SHIFT + Square::BIT_COUNT;
			static constexpr int BIT_COUNT = SQUARE1_SHIFT + Square::BIT_COUNT;
			static constexpr size_t SIZE = 1 << BIT_COUNT;

		public:
			static const util::Table<BetweenTable::SIZE, BitBoard::Type, BetweenTable::SQUARE1_SHIFT, BetweenTable::SQUARE2_SHIFT> getItem;
	};
}

#endif

