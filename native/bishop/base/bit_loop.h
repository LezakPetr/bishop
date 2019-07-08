
#ifndef _BISHOP_BASE_BIT_LOOP_H_
#define _BISHOP_BASE_BIT_LOOP_H_


#include "bit_board.h"


namespace bishop::base {
	/**
	 * BitLoop is auxiliary class that returns series of squares from given bit board with set bit.
	 * @author Ing. Petr Ležák
	 */
	class BitLoop {
		private:
			// Board with still unread squares
			BitBoard::Type board;

		public:
			/**
			 * Initializes loop above given bit board.
			 * @param board bit board
			 */
			inline BitLoop (const BitBoard::Type board_):
				board(board_)
	       		{
			}

			/**
			 * Checks if there is some another square with set bit.
			 * @return true if there is another square, false if not
			 */
			inline bool hasNextSquare() {
				return board != 0;
			}
	
			/**
			 * Returns next set bit.
			 * If there is no more bit set result is undefined.
			 * @return square with set bit
			 */
			inline Square::Type getNextSquare() {
				const Square::Type square = BitBoard::getFirstSquare(board);
				board &= board - 1;
			
				return square;
			}
	
	};
	
}

#endif

