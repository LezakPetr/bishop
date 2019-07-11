
#ifndef _BISHOP_BASE_FIGURE_ATTACK_TABLE_H_
#define _BISHOP_base_FIGURE_ATTACK_TABLE_H_


#include "square.h"
#include "bit_board.h"
#include "piece_type.h"


namespace bishop::base {
	class FigureAttackTable {
		private:
			static constexpr int SQUARE_SHIFT = 0;
			static constexpr int PIECE_TYPE_SHIFT = SQUARE_SHIFT + Square::BIT_COUNT;
			static constexpr int SIZE = PieceType::FIGURE_LAST * (1 << PIECE_TYPE_SHIFT);

			typedef Table<FigureAttackTable::SIZE, BitBoard::Type, FigureAttackTable::PIECE_TYPE_SHIFT, FigureAttackTable::SQUARE_SHIFT> TheTable;

			/**
			 * Initializes table of short move figure attacks.
			 * @param pieceType type of piece
			 * @param table created table to fill
			 */
			static void initializeShortMoveFigureAttacks(const PieceType::Type pieceType, TheTable &table);

			/**
			 * Initializes table of long move figure attacks.
			 * @param pieceType type of piece
			 * @param table created table to fill
			 */
			static void initializeLongMoveFigureAttacks(const PieceType::Type pieceType, TheTable &table);
		
		public:	
			static const TheTable getItem;
	
	};

}

#endif

