#ifndef _BISHOP_BASE_BIT_BOARD_H_
#define _BISHOP_BASE_BIT_BOARD_H_

#include "square.h"
#include "color.h"
#include "../../util/table.h"

#include <cstdint>


using namespace util;


namespace bishop::base {

	class BitBoard {
		public:
			typedef uint64_t Type;
	
			static constexpr BitBoard::Type EMPTY = 0L;
			static constexpr BitBoard::Type FULL = 0xFFFFFFFFFFFFFFFFL;

		private:
			static constexpr int NEAR_KING_SQUARE_COUNT_TABLE_BITS = 12;
			static constexpr int NEAR_KING_SQUARE_COUNT_TABLE_SIZE = 1 << NEAR_KING_SQUARE_COUNT_TABLE_BITS;

			// Before multiplication: abc_____def_____ghi
			// After  multiplication: abc_ghi_def_
			static constexpr int NEAR_KING_SQUARE_COUNT_TABLE_SHIFT = Square::COUNT - NEAR_KING_SQUARE_COUNT_TABLE_BITS;
			static constexpr BitBoard::Type NEAR_KING_SQUARE_COUNT_TABLE_COEFF = 0x1001001001001001ULL;

			static Table<NEAR_KING_SQUARE_COUNT_TABLE_SIZE, uint8_t, 0> NEAR_KING_SQUARE_COUNT_TABLE;

		public:
			/**
			 * Returns bit board with just one set bit on given square.
			 * @param square square
			 * @return mask of square
			 */
			static inline BitBoard::Type getSquareMask (const Square::Type square) {
				return ((BitBoard::Type) 1) << square;
			}
			
			static inline BitBoard::Type of() {
				return BitBoard::EMPTY;
			}

			template<typename... Args>
			static inline BitBoard::Type of(const Square::Type square, Args... args) {
				return BitBoard::getSquareMask(square) | BitBoard::of(args...);
			}

			template<typename Iter>
			static BitBoard::Type fromRange	(Iter first, Iter last) {
				BitBoard::Type mask = BitBoard::EMPTY;

				for (Iter it = first; it != last; ++it)
					mask |= BitBoard::getSquareMask (*it);

				return mask;
			}


			static inline int getSquareCount (const BitBoard::Type board) {
				//const BitBoard::Type counterWidth2 = (board         & 0x5555555555555555L) + ((board         & 0xAAAAAAAAAAAAAAAAL) >> 1);

				// 00 = 0 => 0
				// 01 = 1 => 1
				// 10 = 2 => 1
				// 11 = 3 => 2
				// counterWidth2 = board - upper bit shifted down
				const BitBoard::Type counterWidth2 = board - ((board & 0xAAAAAAAAAAAAAAAAL) >> 1);
				const BitBoard::Type counterWidth4 = (counterWidth2 & 0x3333333333333333L) + ((counterWidth2 & 0xCCCCCCCCCCCCCCCCL) >> 2);
				const BitBoard::Type counterWidth8 = (counterWidth4 & 0x0F0F0F0F0F0F0F0FL) + ((counterWidth4 & 0xF0F0F0F0F0F0F0F0L) >> 4);
				const BitBoard::Type count = (int) ((counterWidth8 * 0x0101010101010101L) >> 56);

				return count;
			}

			static inline int getSquareCountSparse (const BitBoard::Type board) {
				BitBoard::Type mask = board;
				int count = 0;

				while (mask != 0) {
					mask &= mask - 1;
					count++;
				}

				return count;
			}

			/**
			 * Returns number of squares in given board,
			 * but expects that the board contains only near king squares (with distance <= 1).
			 */
			static inline int getSquareCountNearKing (const BitBoard::Type board) {
				int index = (int) ((board * NEAR_KING_SQUARE_COUNT_TABLE_COEFF) >> NEAR_KING_SQUARE_COUNT_TABLE_SHIFT);

				return NEAR_KING_SQUARE_COUNT_TABLE(index);
			}

			static inline bool hasSingleSquare (const BitBoard::Type board) {
				return board != 0 && (board & (board - 1)) == 0;
			}

			static inline bool hasAtLeastTwoSquares (const BitBoard::Type board) {
				return (board & (board - 1)) != 0;
			}

		private:	
			static constexpr BitBoard::Type FIRST_SQUARE_TABLE_COEFF = 544578837055249459ULL;
			static constexpr int FIRST_SQUARE_TABLE_BITS = Square::BIT_COUNT + 1;
			static constexpr int FIRST_SQUARE_TABLE_SIZE = 1 << FIRST_SQUARE_TABLE_BITS;
			static constexpr int FIRST_SQUARE_TABLE_SHIFT = Square::COUNT - FIRST_SQUARE_TABLE_BITS;
			static Table<FIRST_SQUARE_TABLE_SIZE, Square::SmallType, 0> FIRST_SQUARE_TABLE;

			static inline int getFirstSquareTableIndex (const BitBoard::Type board) {
				const BitBoard::Type firstSetBit = board & -board;

				return (int) ((firstSetBit * FIRST_SQUARE_TABLE_COEFF) >> FIRST_SQUARE_TABLE_SHIFT);
			}

		public:
			/**
			 * Returns first set square on the board.
			 * @param board board
			 * @return first set square or Square.NONE
			 */
			static Square::Type getFirstSquare(const BitBoard::Type board) {
				const int index = getFirstSquareTableIndex(board);

				return FIRST_SQUARE_TABLE(index);
			}

			/**
			 * Returns last set square on the board.
			 * @param board board
			 * @return last set square or Square.NONE
			 */
		/*	public static int getLastSquare(final long board) {
				if (board != 0)
					return 63 - Long.numberOfLeadingZeros(board);
				else
					return Square.NONE;
			}*/

			/**
			 * Returns board with mirrored ranks.
			 * @param board board to mirror
			 * @return mirrored board
			 */
			static inline BitBoard::Type getMirrorBoard(const BitBoard::Type board) {
				BitBoard::Type result = board;
				
				result = ((result & 0xFF00FF00FF00FF00L) >> 8) | ((result & 0x00FF00FF00FF00FFL) << 8);
				result = ((result & 0xFFFF0000FFFF0000L) >> 16) | ((result & 0x0000FFFF0000FFFFL) << 16);
				result = ((result & 0xFFFFFFFF00000000L) >> 32) | ((result & 0x00000000FFFFFFFFL) << 32);
				
				return result;
			}

			/**
			 * Returns n-th lowest set square.
			 * @param possibleSquares mask with squares
			 * @param index index of the square
			 * @return square
			 */
			static inline Square::Type getNthSquare(const BitBoard::Type possibleSquares, const int index) {
				BitBoard::Type mask = possibleSquares;

				for (int i = 0; i < index; i++)
					mask &= mask - 1;
				
				return getFirstSquare(mask);
			}

			/**
			 * Returns index of given square. In is the index of the square if set squares would be sorted.
			 * @param possibleSquares mask of square
			 * @param square square, it must be set in possibleSquares
			 * @return index of given square
			 */
			static inline int getSquareIndex(const BitBoard::Type possibleSquares, const Square::Type square) {
				const BitBoard::Type squareMask = getSquareMask(square);
				const BitBoard::Type preSquareMask = squareMask - 1;   // Contains 1 on squares lower than given square
				
				return getSquareCount(possibleSquares & preSquareMask);
			}

			static inline BitBoard::Type extendForward (const BitBoard::Type mask) {
				BitBoard::Type result = mask;
				
				result |= result << File::LAST;
				result |= result << (2 * File::LAST);
				result |= result << (4 * File::LAST);
				
				return result;
			}

			static inline BitBoard::Type extendBackward (const BitBoard::Type mask) {
				BitBoard::Type result = mask;
				
				result |= result >> File::LAST;
				result |= result >> (2 * File::LAST);
				result |= result >> (4 * File::LAST);
				
				return result;
			}

			static inline BitBoard::Type extendForwardByColor (const Color::Type color, const BitBoard::Type mask) {
				if (color == Color::WHITE)
					return extendForward(mask);
				else
					return extendBackward(mask);
			}

			static inline BitBoard::Type extendForwardByColorWithoutItself (const Color::Type color, const BitBoard::Type mask) {
				if (color == Color::WHITE)
					return BitBoard::extendForward(mask) << File::LAST;
				else
					return BitBoard::extendBackward(mask) >> File::LAST;
			}

			static inline bool containsSquare(const BitBoard::Type mask, const Square::Type square) {
				return ((mask >> square) & 0x01) != 0;
			}
	};

}

#endif

