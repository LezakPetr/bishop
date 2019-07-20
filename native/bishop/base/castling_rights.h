

#ifndef _BISHOP_BASE_CASTLING_RIGHTS_H_
#define _BISHOP_BASE_CASTLING_RIGHTS_H_


#include "color.h"
#include "square.h"
#include "bit_board.h"
#include "castling_type.h"


namespace bishop::base {
	/**
	 * Representation of castling rights for both white and black player.
	 * @author Ing. Petr Ležák
	 */
	class CastlingRights {
		public:
			typedef u_int8_t Index;

		private:
			// Rights for castling. Each right is represented by one bit of this number.
			Index rights;
	
			// Full rights of both sides.
			static constexpr Index FULL_RIGHTS = 0x0F;

		public:
			static constexpr Index FIRST_INDEX = 0;
			static constexpr Index LAST_INDEX = FULL_RIGHTS + 1;

			// Mask of all squares that affects castling rights
			static constexpr BitBoard::Type AFFECTED_SQUARES = BitBoard::of(
				Square::A1, Square::E1, Square::H1, Square::A8, Square::E8, Square::H8
			);


		private:
			// Table contains mask of castling rights that are preserved when given square changes its content.
			static const Table<Square::COUNT, CastlingRights::Index, 0> TABLE_SQUARE_RIGHT_MASK;

			// Table contains mask of castling rights for given color.
			static const Table<Color::COUNT, CastlingRights::Index, 0> TABLE_COLOR_RIGHT_MASK;
	
	
	
	/*private static long initializeAffectedSquares() {
		long mask = 0;
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			if (TABLE_SQUARE_RIGHT_MASK[square] != FULL_RIGHTS)
				mask |= BitBoard.getSquareMask(square);
		}
		
		return mask;
	}*/


		public:
			/**
			 * Returns index of given castling right.
			 * @param color color of player
			 * @param type type of castling
			 * @return mask with one bit set corresponding to required right
			 */
			static Index getMaskOfRight (const Color::Type color, const CastlingType::Type type) {
				return (Index) 1 << ((color << 1) + type);
			}

			/**
			 * Default constructor - creates cleared rights.
			 */
			CastlingRights() {
				clearRights();
			}
      
			/**
			 * Initializes rights to full castling rights.
			 */
			void setFullRights() {
				rights = FULL_RIGHTS;
			}

			/**
			 * Clears castling rights.
			 */
			void clearRights() {
				rights = 0;
			}

			/**
			 * Returns right to make castling with given color and type.
			 * @param color color of player
			 * @param type type of right
			 * @return true if given player has right for given castling, false if not
			 */
			bool isRight (const Color::Type color, const CastlingType::Type type) {
				return (rights & getMaskOfRight (color, type)) != 0;
			}
    
			/**
			 * Checks if there is some right for castling of player of given color.
			 * @param color color of player
			 * @return true if given player has right for some castling, false if not
			 */
			bool isRightForColor (const Color::Type color) {
				return (rights & TABLE_COLOR_RIGHT_MASK(color)) != 0;
			}

			/**
			 * Checks if set of rights is empty e.g. if there is no right for castling.
			 * @return true if set of rights is empty, false if not
			 */
			bool isEmpty() {
				return rights == 0;
			}

			/**
			 * Sets right for castling to given value.
			 * @param color color of player
			 * @param type type of castling
			 * @param isRight true if required right should be set, false if it should be cleared
			 */
			void setRight (const Color::Type color, const CastlingType::Type type, const bool isRight) {
				const Index mask = getMaskOfRight (color, type);

				if (isRight)
					rights |= mask;
				else
					rights &= ~mask;
			}

			/**
			 * Updates (drops) right caused by changing content of given square.
			 * @param square square that was changed
			 */
			void updateAfterSquareChange (const Square::Type square) {
				rights &= TABLE_SQUARE_RIGHT_MASK(square);
			}

			/**
			 * Drops rights for castling of player with given color.
			 * @param color color of player
			 */
			void dropRightsForColor (const Color::Type color) {
				rights &= ~TABLE_COLOR_RIGHT_MASK(color);
			}
    
			/**
			 * Assigns right with swapped white and black side.
			 * @param orig original rights
			 */
			void assignMirror(const CastlingRights orig) {
				rights = (Index) (((orig.rights & TABLE_COLOR_RIGHT_MASK(Color::WHITE)) << 2) | ((orig.rights & TABLE_COLOR_RIGHT_MASK(Color::BLACK)) >> 2));
			}
        
			/**
			 * Returns index of this rights.
			 * @return rights index
			 */
			Index getIndex() {
				return rights;
			}

			/**
			 * Sets index of this rights.
			 * @param index right index
			 */
			void setIndex(const Index index) {
				rights = index;
			}
	};
}

#endif

