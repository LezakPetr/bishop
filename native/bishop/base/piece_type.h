
#ifndef _BISHOP_BASE_PIECE_TYPE_H_
#define _BISHOP_BASE_PIECE_TYPE_H_

#include <cstdint>

namespace bishop::base {

	class PieceType {
		public:
			typedef int_fast8_t Type;

			static constexpr PieceType::Type FIRST = 0;
	
			static constexpr PieceType::Type KING = 0;
			static constexpr PieceType::Type QUEEN = 1;
			static constexpr PieceType::Type ROOK = 2;
			static constexpr PieceType::Type BISHOP = 3;
			static constexpr PieceType::Type KNIGHT = 4;
			static constexpr PieceType::Type PAWN = 5;
	
			static constexpr PieceType::Type LAST = 6;
			static constexpr int COUNT = LAST - FIRST;

			static constexpr PieceType::Type NONE = 6;   // I must use value that fits into 3 bits because Move needs it
	
			// Subinterval of figures (pieces without pawn)
			static constexpr PieceType::Type FIGURE_FIRST = 0;
			static constexpr PieceType::Type FIGURE_LAST = 5;
			static constexpr int FIGURE_COUNT = FIGURE_LAST - FIGURE_FIRST;
	
			// Subinterval of figures that can pawn promote to
			static constexpr PieceType::Type PROMOTION_FIGURE_FIRST = 1;
			static constexpr PieceType::Type PROMOTION_FIGURE_LAST  = 5;
			static constexpr int PROMOTION_FIGURE_COUNT = PROMOTION_FIGURE_LAST - PROMOTION_FIGURE_FIRST;
	
			// Subinterval of variable pieces (all pieces without king which is permanent).
			static constexpr PieceType::Type VARIABLE_FIRST = 1;
			static constexpr PieceType::Type VARIABLE_LAST = 6;
			static constexpr int VARIABLE_COUNT = PieceType::VARIABLE_LAST - PieceType::VARIABLE_FIRST;
	
			/**
			 * Checks if given piece type is valid.
			 * @param pieceType piece type
			 * @return true if piece type is valid, false if not
			 */
			static inline bool isValid (const PieceType::Type pieceType) {
				return pieceType >= FIRST && pieceType < LAST;
			}	
	
			/**
			 * Checks if given pieceType is figure.
			 * @param pieceType type of piece
			 * @return true if given piece type is figure, false if not or if piece type is not valid
			 */
			static inline bool isFigure (const PieceType::Type pieceType) {
				return pieceType >= FIGURE_FIRST && pieceType < FIGURE_LAST;
			}	

			/**
			 * Checks if given pieceType is short moving figure.
			 * @param pieceType type of piece
			 * @return true if given piece type is short moving figure, false if not or if piece type is not valid
			 */
			static inline bool isShortMovingFigure (const PieceType::Type pieceType) {
			    return pieceType == KING || pieceType == KNIGHT;
			}

			static inline bool isVariablePiece(const PieceType::Type pieceType) {
				return pieceType >= VARIABLE_FIRST && pieceType < VARIABLE_LAST;
			}

			static inline bool isPromotionFigure(const PieceType::Type pieceType) {
				return pieceType >= PROMOTION_FIGURE_FIRST && pieceType < PROMOTION_FIGURE_LAST;
			}
	};
}

#endif

