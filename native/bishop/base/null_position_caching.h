
#ifndef _BISHOP_BASE_NULL_POSITION_CACHING_H_
#define _BISHOP_BASE_NULL_POSITION_CACHING_H_

#include "color.h"
#include "piece_type.h"
#include "square.h"

namespace bishop::base {
	class NullPositionCaching {
		public:
			void movePiece(const Color::Type color, const PieceType::Type pieceType, const Square::Type beginSquare, const Square::Type targetSquare) {
			}

			void addPiece(const Color::Type color, const PieceType::Type pieceType, const Square::Type square) {
			}

			void removePiece(const Color::Type color, const PieceType::Type pieceType, const Square::Type square) {
			}

			void swapOnTurn() {
			}

			void changeEpFile(const Square::Type from, const Square::Type to) {
			}

			void changeCastlingRights(const Square::Type fromIndex, const Square::Type toIndex) {
			}
	};

}

#endif

