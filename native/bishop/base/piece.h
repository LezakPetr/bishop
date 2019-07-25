
#ifndef _BISHOP_BASE_PIECE_H_
#define _BISHOP_BASE_PIECE_H_


#include "color.h"
#include "piece_type.h"
#include <cstddef>


namespace bishop::base {
	class Piece {
		private:
			unsigned int color: Color::BIT_COUNT;
			unsigned int pieceType: PieceType::BIT_COUNT;

			constexpr Piece (const Color::Type color_, const PieceType::Type pieceType_):
				color(color_),
				pieceType(pieceType_)
			{
			}

		public:
/*			static constexpr Piece WHITE_PAWN = Piece(Color::WHITE, PieceType::PAWN);
			static constexpr Piece BLACK_PAWN(Color::BLACK, PieceType::PAWN);
			static constexpr Piece WHITE_KNIGHT(Color::WHITE, PieceType::KNIGHT);
			static constexpr Piece BLACK_KNIGHT(Color::BLACK, PieceType::KNIGHT);
			static constexpr Piece WHITE_BISHOP(Color::WHITE, PieceType::BISHOP);
			static constexpr Piece BLACK_BISHOP(Color::BLACK, PieceType::BISHOP);
			static constexpr Piece WHITE_ROOK(Color::WHITE, PieceType::ROOK);
			static constexpr Piece BLACK_ROOK(Color::BLACK, PieceType::ROOK);
			static constexpr Piece WHITE_QUEEN(Color::WHITE, PieceType::QUEEN);
			static constexpr Piece BLACK_QUEEN(Color::BLACK, PieceType::QUEEN);
			static constexpr Piece WHITE_KING(Color::WHITE, PieceType::KING);
			static constexpr Piece BLACK_KING(Color::BLACK, PieceType::KING);

			static constexpr Piece Piece EMPTY(Color::WHITEm PieceType::NONE);*/
	
			/**
			 * Returns color of piece.
			 * @return color of piece
			 */
			Color::Type getColor() const {
				assert (!isEmpty());

				return color;
			}
	
			/**
			 * Returns type of piece.
			 * @return type of piece
			 */
			PieceType::Type getPieceType() const {
				assert (!isEmpty());

				return pieceType;
			}

			bool isEmpty() const {
				return pieceType == PieceType::NONE;
			}

			/**
			 * Returns piece with given color and type.
			 * @param color color of piece
			 * @param pieceType type of piece
			 * @return precreated instance of piece
			 */
			static Piece withColorAndType (const Color::Type color, const PieceType::Type pieceType) {
				assert (Color::isValid (color));
				assert (PieceType::isValid (pieceType));

				return Piece(color, pieceType);
			}
	
			BitBoard::Type getAttackedSquares (const Square::Type square) {
				if (pieceType == PieceType::PAWN)
					return PawnAttackTable::getItem(color, square);
				else
					return FigureAttackTable::getItem(pieceType, square);
			}
	
			static constexpr size_t LAST_PROMOTION_FIGURE_INDEX = (PieceType::PROMOTION_FIGURE_LAST - PieceType::PROMOTION_FIGURE_FIRST) << Color::BIT_COUNT;
	
			static constexpr size_t getPromotionFigureIndex (const Color::Type color, const PieceType::Type pieceType) {
				return ((pieceType - PieceType::PROMOTION_FIGURE_FIRST) << Color::BIT_COUNT) + color;
			}
	};

}

#endif

