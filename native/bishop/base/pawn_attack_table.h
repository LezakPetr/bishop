
#ifndef _BISHOP_BASE_PAWN_ATTACK_TABLE_H_
#define _BISHOP_BASE_PAWN_ATTACK_TABLE_H_


#include "square.h"
#include "bit_board.h"
#include "../../util/table.h"

#include <cstddef>


namespace bishop::base {

	class PawnAttackTable {
		private:	
			static constexpr int SQUARE_SHIFT = 0;
			static constexpr int COLOR_SHIFT = SQUARE_SHIFT + Square::BIT_COUNT;
			static constexpr int BIT_COUNT = COLOR_SHIFT + Color::BIT_COUNT;
			static constexpr size_t SIZE = 1 << BIT_COUNT;
		
		public:
			static const Table<PawnAttackTable::SIZE, BitBoard::Type, PawnAttackTable::COLOR_SHIFT, PawnAttackTable::SQUARE_SHIFT> getItem;
	
	};
}

#endif

