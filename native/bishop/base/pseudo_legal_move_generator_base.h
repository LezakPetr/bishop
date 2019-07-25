

#ifndef _BISHOP_BASE_PSEUDO_LEGAL_MOVE_GENERATOR_BASE_H_
#define _BISHOP_BASE_PSEUDO_LEGAL_MOVE_GENERATOR_BASE_H_


#include "bit_loop.h"


namespace bishop::base {

	template<typename Pos, typename Walker>
	class PseudoLegalMoveGeneratorBase {
		private:
			bool generateMovesOfPiece[PieceType::LAST];
			
		protected:
			Pos & position;
			Walker & walker;

		protected:
			PseudoLegalMoveGeneratorBase(Pos & position_, Walker & walker_):
				position (position_),
				walker (walker_)
			{
				::std::fill_n (generateMovesOfPiece, PieceType::LAST, true);
			}

    			// Generates en-passant moves.
			// Returns if generation should continue.
			bool generateEnPassantMoves() {
				const File::Type epFile = position.getEpFile();

				if (epFile == File::NONE)
					return true;

				const Color::Type onTurn = position.getOnTurn();
				const Color::Type oppositeColor = Color::getOppositeColor(onTurn);
				const BitBoard::Type pawnMask = position.getPiecesMask (onTurn, PieceType::PAWN);
				const Square::Type epSquare = BoardConstants::getEpSquare(oppositeColor, epFile);
				const BitBoard::Type possibleBeginSquares = BoardConstants::getConnectedPawnSquareMask(epSquare) & pawnMask;
				
				Move move;
				move.setMovingPieceType(PieceType::PAWN);

			    	for (BitLoop loop(possibleBeginSquares); loop.hasNextSquare(); ) {
					const Square::Type beginSquare = loop.getNextSquare();

					move.setBeginSquare(beginSquare);
					move.finishEnPassant(BoardConstants::getEpTargetSquare(oppositeColor, epFile));

					if (!walker.processMove(move))
						return false;
				}

				return true;
			}

		public:
			inline bool getGenerateMovesOfPiece (const PieceType::Type pieceType) const {
				return generateMovesOfPiece[pieceType];
			}

			inline void getGenerateMovesOfPiece (const PieceType::Type pieceType, const bool generate) {
				generateMovesOfPiece[pieceType] = generate;
			}

	};

}

#endif

