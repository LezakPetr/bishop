
#ifndef _BISHOP_BASE_LINE_ATTACK_TABLE_H_
#define _BISHOP_BASE_LINE_ATTACK_TABLE_H_


#include "line_indexer.h"
#include "square.h"


namespace bishop::base {
	/**
	 * This table contains squares attacked from some square with some occupancy.
	 * @author Ing. Petr Ležák
	 */
	class LineAttackTable {
		private:
			typedef Table<LineIndexer::LAST_INDEX, BitBoard::Type, 0> TheTable;

			static void initializeTable (TheTable &table, const int betweenCount);

			static BitBoard::Type calculateAttackMask (const Square::Type square, const BitBoard::Type cross, const BitBoard::Type occupancy, const int betweenCount);
		public:
			/**
			 * Returns mask of attacked squares.
			 * @param index line index
			 * @return mask of attacked squares
			 */
			static const TheTable getAttackMask;

			/**
			 * Returns mask of squares blocked by one piece.
			 * @param index line index
			 * @return mask of attacked squares with one blocking piece 
			 */
			static const TheTable getPinMask;
	
			static BitBoard::Type getLineMask (const Square::Type square, const FileRankOffset offset);
	};
}

#endif

