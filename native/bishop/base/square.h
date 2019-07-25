#ifndef _BISHOP_BASE_SQUARE_H_
#define _BISHOP_BASE_SQUARE_H_

#include "file.h"
#include "rank.h"
#include "color.h"

#include <cstdint>
#include <string>


namespace bishop::base {
	
	class Square {
		public:
			typedef int_fast8_t Type;
			typedef int8_t SmallType;
			typedef int_fast8_t Difference;

			static constexpr Square::Type FIRST = 0;
			
			static constexpr Square::Type A1 =  0;
			static constexpr Square::Type B1 =  1;
			static constexpr Square::Type C1 =  2;
			static constexpr Square::Type D1 =  3;
			static constexpr Square::Type E1 =  4;
			static constexpr Square::Type F1 =  5;
			static constexpr Square::Type G1 =  6;
			static constexpr Square::Type H1 =  7;
			
			static constexpr Square::Type A2 =  8;
			static constexpr Square::Type B2 =  9;
			static constexpr Square::Type C2 = 10;
			static constexpr Square::Type D2 = 11;
			static constexpr Square::Type E2 = 12;
			static constexpr Square::Type F2 = 13;
			static constexpr Square::Type G2 = 14;
			static constexpr Square::Type H2 = 15;

			static constexpr Square::Type A3 = 16;
			static constexpr Square::Type B3 = 17;
			static constexpr Square::Type C3 = 18;
			static constexpr Square::Type D3 = 19;
			static constexpr Square::Type E3 = 20;
			static constexpr Square::Type F3 = 21;
			static constexpr Square::Type G3 = 22;
			static constexpr Square::Type H3 = 23;

			static constexpr Square::Type A4 = 24;
			static constexpr Square::Type B4 = 25;
			static constexpr Square::Type C4 = 26;
			static constexpr Square::Type D4 = 27;
			static constexpr Square::Type E4 = 28;
			static constexpr Square::Type F4 = 29;
			static constexpr Square::Type G4 = 30;
			static constexpr Square::Type H4 = 31;

			static constexpr Square::Type A5 = 32;
			static constexpr Square::Type B5 = 33;
			static constexpr Square::Type C5 = 34;
			static constexpr Square::Type D5 = 35;
			static constexpr Square::Type E5 = 36;
			static constexpr Square::Type F5 = 37;
			static constexpr Square::Type G5 = 38;
			static constexpr Square::Type H5 = 39;

			static constexpr Square::Type A6 = 40;
			static constexpr Square::Type B6 = 41;
			static constexpr Square::Type C6 = 42;
			static constexpr Square::Type D6 = 43;
			static constexpr Square::Type E6 = 44;
			static constexpr Square::Type F6 = 45;
			static constexpr Square::Type G6 = 46;
			static constexpr Square::Type H6 = 47;
			
			static constexpr Square::Type A7 = 48;
			static constexpr Square::Type B7 = 49;
			static constexpr Square::Type C7 = 50;
			static constexpr Square::Type D7 = 51;
			static constexpr Square::Type E7 = 52;
			static constexpr Square::Type F7 = 53;
			static constexpr Square::Type G7 = 54;
			static constexpr Square::Type H7 = 55;

			static constexpr Square::Type A8 = 56;
			static constexpr Square::Type B8 = 57;
			static constexpr Square::Type C8 = 58;
			static constexpr Square::Type D8 = 59;
			static constexpr Square::Type E8 = 60;
			static constexpr Square::Type F8 = 61;
			static constexpr Square::Type G8 = 62;
			static constexpr Square::Type H8 = 63;

			static constexpr Square::Type LAST = 64;
			
			static constexpr Square::Type NONE = 100;
			static constexpr int BIT_COUNT = 6;
			
			// Range of squares where pawn can be placed
			static constexpr Square::Type FIRST_PAWN_SQUARE = Square::A2;
			static constexpr Square::Type LAST_PAWN_SQUARE = Square::H7 + 1;

			static constexpr Square::Difference COUNT = LAST - FIRST;
			

			/**
			 * Checks if given square is valid.
			 * @param square square
			 * @return true if square is valid, false if not
			 */
			static inline bool isValid (const Square::Type square) {
				return square >= FIRST && square < LAST;
			}

			/**
			 * Returns file of given square.
			 * @param square square coordinate
			 * @return file of square
			 */
			static inline File::Type getFile (const Square::Type square) {
				return square & 0x07;
			}

			/**
			 * Returns rank of given square.
			 * @param square square coordinate
			 * @return rank of square
			 */
			static inline Rank::Type getRank (const Square::Type square) {
				return square >> 3;
			}
			
			/**
			 * Returns square on given file and rank.
			 * @param file file coordinate
			 * @param rank rank coordinate
			 * @return square on given file and rank.
			 */
			static inline Square::Type onFileRank (const File::Type file, const Rank::Type rank) {
				return file + (rank << 3); 
			}
			
			/**
			 * Returns corresponding square from view of opposite side.
			 * @param square square
			 * @return opposite square
			 */
			static inline Square::Type getOppositeSquare (const Square::Type square) {
				// We need to reverse rank:
				// oppositeSquare = ((7 - rank) << 3) | file

				// Replace negation of rank with bit inverse and add one
				// oppositeSquare = ((7 + ~rank + 1) << 3) | file

				// Replace file and rank with parts of square
				// oppositeSquare = ((~(square >>> 3) + 8) << 3) | (square & 0x07)
				// oppositeSquare = (~square & ~0x07) + 64) | (square & 0x07)
				// oppositeSquare = ((~square & ~0x07) | (square & 0x07)) + 64

				// We are inverting all bits of square except lower 3. We can achieve this by single XOR.
				// oppositeSquare = (square ^ ~0x07) + 64
				return (square ^ ~0x7) + 64;
			}

			static Square::Type fromString (::std::string const & str);
	};
			
}

#define FOR_EACH_SQUARE(square) for (::bishop::base::Square::Type square = ::bishop::base::Square::FIRST; square < ::bishop::base::Square::LAST; square++)

#endif

