

#ifndef _BISHOP_BASE_BIT_BOARD_COMBINATOR_H_
#define _BISHOP_BASE_BIT_BOARD_COMBINATOR_H_


#include "bit_board.h"


namespace bishop::base {

	class BitBoardCombinator {
		public:
			typedef int_fast64_t CountType;
			static constexpr int MAX_SQUARE_COUNT = 62;   // Maximal number of squares so CountType does not overflow

		private:
			const BitBoard::Type mask;
			BitBoard::Type combination;
			CountType remainingCombinations;
	
		public:
			inline BitBoardCombinator(const BitBoard::Type mask_):
				mask (mask_),
				combination (BitBoard::EMPTY),
				remainingCombinations (getCombinationCount())
			{
			}

			inline bool hasNextCombination() {
				return remainingCombinations > 0;
			}
	
			inline BitBoard::Type getNextCombination() {
				const BitBoard::Type combinationToReturn = combination;

				// We want then number 'combination' to be incremented by 1 in bits of mask. We can simply add 1
				// to it, but we must ensure that the overflow is carried between non-continuous bits.
				// So we first set the bits that are not in the mask to 1, then we increment it by 1
				// and then we clears the bits out of mask again. So:
				//   combination = (combination + ~mask + 1) & mask
				// Because ~mask + 1 = -mask we have:
				//   combination = (combination - mask) & mask
				// See The Art Of Computer Programming, Volume 1, Fascicle 1: Working with fragmented fields, Equation 84
				combination = (combination - mask) & mask;
				remainingCombinations--;
		
				return combinationToReturn;
			}

			CountType getCombinationCount() {
				const int squareCount = BitBoard::getSquareCount (mask);
				assert (squareCount < MAX_SQUARE_COUNT);

				return ((CountType) 1) << squareCount;
			}
	};
}

#endif

