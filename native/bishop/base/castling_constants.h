

#ifndef _BISHOP_BASE_CASTLING_CONSTANTS_H_
#define _BISHOP_BASE_CASTLING_CONSTANTS_H_


#include "../../util/table.h"
#include "square.h"
#include "bit_board.h"
#include "castling_type.h"


namespace bishop::base {
	class CastlingConstants {
		private:
			BitBoard::Type middleSquareMask;   // Mask of squares between king and rook before castling
			Square::Type rookBeginSquare;   // Begin square of rook in castling
			Square::Type rookTargetSquare;   // Target square of rook in castling
			Square::Type kingBeginSquare;   // Begin square of king in the castling
			Square::Type kingTargetSquare;   // Target squares of king in castling
			BitBoard::Type kingChangeMask;   // Contains changes of king mask for given type of castling
			BitBoard::Type rookChangeMask;   // Contains changes of rook mask for given type of castling
		
		public:
			static const Table<Color::LAST * CastlingType::LAST, CastlingConstants, CastlingType::BIT_COUNT, 0> of;

			CastlingConstants():
				middleSquareMask(BitBoard::EMPTY),
				rookBeginSquare(Square::NONE),
				rookTargetSquare(Square::NONE),
				kingBeginSquare(Square::NONE),
				kingTargetSquare(Square::NONE),
				kingChangeMask(BitBoard::EMPTY),
				rookChangeMask(BitBoard::EMPTY)
			{
			}

			CastlingConstants (const BitBoard::Type middleSquareMask_, const Square::Type rookBeginSquare_, const Square::Type rookTargetSquare_, const Square::Type kingBeginSquare_, const Square::Type kingTargetSquare_):
				middleSquareMask(middleSquareMask_),
				rookBeginSquare(rookBeginSquare_),
				rookTargetSquare(rookTargetSquare_),
				kingBeginSquare(kingBeginSquare_),
				kingTargetSquare(kingTargetSquare_),
				kingChangeMask(BitBoard::of(kingBeginSquare_, kingTargetSquare_)),
				rookChangeMask(BitBoard::of(rookBeginSquare_, rookTargetSquare_))
		       	{
			}
	
			inline BitBoard::Type getMiddleSquareMask() const {
				return middleSquareMask;
			}

			inline Square::Type getRookBeginSquare() const {
				return rookBeginSquare;
			}

			inline Square::Type getKingMiddleSquare() const {
				return rookTargetSquare;
			}

			inline Square::Type getRookTargetSquare() const {
				return rookTargetSquare;
			}

			inline Square::Type getKingBeginSquare() const {
				return kingBeginSquare;
			}

			inline Square::Type getKingTargetSquare() const {
				return kingTargetSquare;
			}

			inline BitBoard::Type getKingChangeMask() const {
				return kingChangeMask;
			}

			inline BitBoard::Type getRookChangeMask() const {
				return rookChangeMask;
			}
	};
}

#endif

