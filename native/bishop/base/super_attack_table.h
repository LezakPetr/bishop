
#ifndef _BISHOP_BASE_SUPER_ATTACK_TABLE_H_
#define _BISHOP_BASE_SUPER_ATTACK_TABLE_H_

#include "square.h"
#include "bit_board.h"
#include "../../util/table.h"


namespace bishop::base {
	/**
	 * Table that contains masks of squares attackable by any piece from given square.
	 */
	class SuperAttackTable {
		public:
			static const Table<Square::LAST, BitBoard::Type, 0> getItem;
	};

}

#endif

