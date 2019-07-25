

#ifndef _BISHOP_BASE_PSEUDO_LEGAL_MOVE_GENERATOR_H_
#define _BISHOP_BASE_PSEUDO_LEGAL_MOVE_GENERATOR_H_


#include "pseudo_legal_move_generator_base.h"
#include "pawn_move_table.h"
#include "pawn_attack_table.h"
#include "super_attack_table.h"
#include "between_table.h"


namespace bishop::base {

	/**
	 * PseudoLegalMoveGenerator generates moves that are legal except it is not verified that the side
	 * that has done the move is not in check.
	 * The generator can be restricted to generate only checking moves. In that case all checking moves must be generated
	 * and also some non-checking ones (it is not perfect).
	 * The generator can also be configured to generate moves that has a chance to stop the check in position. Again,
	 * more moves can be generated. Both options can be used at the same time.  
	 * @author Ing. Petr Ležák
	 */
	template<typename Pos, typename Walker>
	class PseudoLegalMoveGenerator: public PseudoLegalMoveGeneratorBase<Pos, Walker> {
		private:
			static constexpr int MAX_MOVES_IN_POSITION = 321;

			Move move;
	
			// Settings
			bool reduceMovesInCheck;
			bool generateOnlyChecks;
	
			// Cached from position
			Color::Type onTurn;
			Color::Type oppositeColor;
			Square::Type oppositeKingSquare;
	
			// Direct checking masks. These masks are used to mask target squares.
			// If generateOnlyChecks is true - squares from where it is possible to give check to the king not on turn.
			// If generateOnlychecks is false - BitBoard.FULL so target squares are not masked.
			BitBoard::Type orthogonalCheckingMask;
			BitBoard::Type diagonalCheckingMask;
	
			// Indirect checking blockers. This mask is used to mask begin squares.
			// If generateOnlyChecks is true - mask of all own pieces that are the only blockers between own line figure and
			// opposite king.
			// If generateOnlychecks is false - BitBoard.FULL so begin squares are not masked.
			BitBoard::Type indirectCheckingBlockers;
	
			// Generates moves of some figure.
			// Returns if generation should continue.
			bool generateShortMovingFigureMoves(const PieceType::Type figure, const BitBoard::Type possibleTargetSquaresForReductionInCheck) {
				const BitBoard::Type beginSquareMask = this->position.getPiecesMask(onTurn, figure);
    				
    				move.setMovingPieceType(figure);

				// Loop through all squares with our figure
				for (BitLoop beginSquareLoop(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
					const Square::Type beginSquare = beginSquareLoop.getNextSquare();
					move.setBeginSquare(beginSquare);

					const BitBoard::Type unfilteredTargetSquareMask = FigureAttackTable::getItem(figure, beginSquare) & possibleTargetSquaresForReductionInCheck;
					const bool indirectCheck = BitBoard::containsSquare (indirectCheckingBlockers, beginSquare);
					const BitBoard::Type targetSquareMask = (indirectCheck) ? unfilteredTargetSquareMask : unfilteredTargetSquareMask & FigureAttackTable::getItem(figure, oppositeKingSquare);

					// Loop through all target squares
					for (BitLoop targetSquareLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
						const Square::Type targetSquare = targetSquareLoop.getNextSquare();
						const PieceType::Type capturedPieceType = this->position.getPieceTypeOnSquare(targetSquare);

						// Make and process move
						move.finishNormalMove(targetSquare, capturedPieceType);

						if (!this->walker.processMove(move))
							return false;
			    		}
			    	}

			    	return true;
			}
	
			// Generates moves of some figure.
			// Returns if generation should continue.
			bool generateBishopMoves(const BitBoard::Type possibleTargetSquaresForReductionInCheck) {
				const BitBoard::Type beginSquareMask = this->position.getPiecesMask(onTurn, PieceType::BISHOP);
				const BitBoard::Type occupancy = this->position.getOccupancy();
    	
				move.setMovingPieceType(PieceType::BISHOP);

				// Loop through all squares with our figure
				for (BitLoop beginSquareLoop(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
					const Square::Type beginSquare = beginSquareLoop.getNextSquare();
					move.setBeginSquare(beginSquare);
    		
					const LineIndexer::IndexType diagonalIndex = LineIndexer::getLineIndex(CrossDirection::DIAGONAL, beginSquare, occupancy);
					const BitBoard::Type unfilteredTargetSquareMask = LineAttackTable::getAttackMask(diagonalIndex) & possibleTargetSquaresForReductionInCheck;
					const bool indirectCheck = BitBoard::containsSquare (indirectCheckingBlockers, beginSquare);
					const BitBoard::Type targetSquareMask = (indirectCheck) ? unfilteredTargetSquareMask : unfilteredTargetSquareMask & diagonalCheckingMask;

					// Loop through all target squares
					for (BitLoop targetSquareLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
						const Square::Type targetSquare = targetSquareLoop.getNextSquare();
						const PieceType::Type capturedPieceType = this->position.getPieceTypeOnSquare(targetSquare);

						// Make and process move
						move.finishNormalMove(targetSquare, capturedPieceType);

						if (!this->walker.processMove(move))
							return false;
					}
				}

				return true;
			}
    
			// Generates moves of some figure.
			// Returns if generation should continue.
			bool generateRookMoves(const BitBoard::Type possibleTargetSquaresForReductionInCheck) {
				const BitBoard::Type beginSquareMask = this->position.getPiecesMask(onTurn, PieceType::ROOK);
				const BitBoard::Type occupancy = this->position.getOccupancy();
    	
				move.setMovingPieceType(PieceType::ROOK);

				// Loop through all squares with our figure
				for (BitLoop beginSquareLoop(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
					const Square::Type beginSquare = beginSquareLoop.getNextSquare();
					move.setBeginSquare(beginSquare);
    		
					const LineIndexer::IndexType orthogonalIndex = LineIndexer::getLineIndex(CrossDirection::ORTHOGONAL, beginSquare, occupancy);
					const BitBoard::Type unfilteredTargetSquareMask = LineAttackTable::getAttackMask(orthogonalIndex) & possibleTargetSquaresForReductionInCheck;
					const bool indirectCheck = BitBoard::containsSquare (indirectCheckingBlockers, beginSquare);
					const BitBoard::Type targetSquareMask = (indirectCheck) ? unfilteredTargetSquareMask : unfilteredTargetSquareMask & orthogonalCheckingMask;

					// Loop through all target squares
					for (BitLoop targetSquareLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
						const Square::Type targetSquare = targetSquareLoop.getNextSquare();
						const PieceType::Type capturedPieceType = this->position.getPieceTypeOnSquare(targetSquare);

						// Make and process move
						move.finishNormalMove(targetSquare, capturedPieceType);

						if (!this->walker.processMove(move))
							return false;
					}
				}

				return true;
			}
    
			// Generates moves of some figure.
			// Returns if generation should continue.
			bool generateQueenMoves(const BitBoard::Type possibleTargetSquaresForReductionInCheck) {
 				const BitBoard::Type beginSquareMask = this->position.getPiecesMask(onTurn, PieceType::QUEEN);
				const BitBoard::Type occupancy = this->position.getOccupancy();
    	
				move.setMovingPieceType(PieceType::BISHOP);
				move.setMovingPieceType(PieceType::QUEEN);

				// Loop through all squares with our figure
				for (BitLoop beginSquareLoop(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
					const Square::Type beginSquare = beginSquareLoop.getNextSquare();
					move.setBeginSquare(beginSquare);
    		
					const LineIndexer::IndexType diagonalIndex = LineIndexer::getLineIndex(CrossDirection::DIAGONAL, beginSquare, occupancy);
					const BitBoard::Type diagonalMask = LineAttackTable::getAttackMask(diagonalIndex);

					const LineIndexer::IndexType orthogonalIndex = LineIndexer::getLineIndex(CrossDirection::ORTHOGONAL, beginSquare, occupancy);
					const BitBoard::Type orthogonalMask = LineAttackTable::getAttackMask(orthogonalIndex);
    		
					const BitBoard::Type unfilteredTargetSquareMask = (diagonalMask | orthogonalMask) & possibleTargetSquaresForReductionInCheck;
					const bool indirectCheck = BitBoard::containsSquare (indirectCheckingBlockers, beginSquare);
					const BitBoard::Type targetSquareMask = (indirectCheck) ? unfilteredTargetSquareMask : unfilteredTargetSquareMask & (diagonalCheckingMask | orthogonalCheckingMask);
    		
					// Loop through all target squares
					for (BitLoop targetSquareLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
						const Square::Type targetSquare = targetSquareLoop.getNextSquare();
						const PieceType::Type capturedPieceType = this->position.getPieceTypeOnSquare(targetSquare);

						// Make and process move
						move.finishNormalMove(targetSquare, capturedPieceType);

						if (!this->walker.processMove(move))
							return false;
					}
				}

				return true;
			}

			// Generates moves of pawns.
			// Returns if generation should continue.
			bool generatePawnMoves(const BitBoard::Type possibleTargetSquaresForReductionInCheck) {
				BitBoard::Type beginSquareMask = this->position.getPiecesMask(onTurn, PieceType::PAWN);
    	
				if (generateOnlyChecks || reduceMovesInCheck) {
					// allowedDirectCheckingTargetSquareMask are those target squares that complies to
					// possibleTargetSquaresForReductionInCheck and where direct check is done if forced
					BitBoard::Type allowedDirectCheckingTargetSquareMask = possibleTargetSquaresForReductionInCheck;
    		
					if (generateOnlyChecks) {
						// If we want to generate only checks then we restricts the target squares to those where it is possible to give check
						// by the move. Promotion is always taken as a potential checking move.
						const BitBoard::Type promotionRankMask = BoardConstants::getRankMask(BoardConstants::getPawnPromotionRank(onTurn));
						const BitBoard::Type directCheckingTargetSquares = PawnAttackTable::getItem(oppositeColor, oppositeKingSquare) | promotionRankMask;
						allowedDirectCheckingTargetSquareMask &= directCheckingTargetSquares;
					}
	
					const BitBoard::Type directCheckingMoveSourceSquares = calculateMoveBeginSquaresFromTargetSquares(allowedDirectCheckingTargetSquareMask);
					const BitBoard::Type directCheckingCaptureBeginSquares = BoardConstants::getPawnsAttackedSquares(oppositeColor, allowedDirectCheckingTargetSquareMask);
    		
					beginSquareMask &= (directCheckingMoveSourceSquares | directCheckingCaptureBeginSquares | indirectCheckingBlockers);
				}
    	
				const BitBoard::Type occupancy = this->position.getOccupancy();
				const BitBoard::Type opponentSquares = this->position.getColorOccupancy(oppositeColor);

				move.setMovingPieceType(PieceType::PAWN);

				// Loop through all squares with our pawns
				for (BitLoop beginSquareLoop(beginSquareMask); beginSquareLoop.hasNextSquare(); ) {
					const Square::Type beginSquare = beginSquareLoop.getNextSquare();
					move.setBeginSquare(beginSquare);
    		
					const BitBoard::Type moveMask = PawnMoveTable::getItem(onTurn, beginSquare) & ~occupancy;
					const BitBoard::Type captureMask = PawnAttackTable::getItem(onTurn, beginSquare) & opponentSquares;
					const BitBoard::Type targetSquareMask = (moveMask | captureMask) & possibleTargetSquaresForReductionInCheck;

					// Loop through all target squares
					for (BitLoop targetSquareLoop(targetSquareMask); targetSquareLoop.hasNextSquare(); ) {
						const Square::Type targetSquare = targetSquareLoop.getNextSquare();

						if ((BetweenTable::getItem(beginSquare, targetSquare) & occupancy) == 0) {
							const PieceType::Type capturedPieceType = this->position.getPieceTypeOnSquare(targetSquare);

							// Make and process move
							if ((BoardConstants::RANK_18_MASK & BitBoard::getSquareMask(targetSquare)) != 0) {
								// Pawn promotion
								for (PieceType::Type promotionFigure = PieceType::PROMOTION_FIGURE_FIRST; promotionFigure < PieceType::PROMOTION_FIGURE_LAST; promotionFigure++) {
									move.finishPromotion(targetSquare, capturedPieceType, promotionFigure);

									if (!this->walker.processMove(move))
										return false;
								}
							}
							else {
								// Normal move
								move.finishNormalMove (targetSquare, capturedPieceType);
	
								if (!this->walker.processMove(move))
									return false;
							}
						}
					}
				}
	
				return true;
			}

			BitBoard::Type calculateMoveBeginSquaresFromTargetSquares(const BitBoard::Type targetSquareMask) {
				if (onTurn == Color::WHITE) {
					return (targetSquareMask >> File::COUNT) |   // Move by one rank
					       ((targetSquareMask & BoardConstants::RANK_4_MASK) >> (2*File::COUNT));   // Move by two ranks
				}
				else {
					return (targetSquareMask << File::COUNT) |   // Move by one rank
					       ((targetSquareMask & BoardConstants::RANK_5_MASK) << (2*File::COUNT));   // Move by two ranks
				}
			}

			// Generates castling moves.
			// Returns if generation should continue.
			bool generateCastlingMoves() {
				const CastlingRights castlingRights = this->position.getCastlingRights();

				// If no castling is permitted skip all tests
				if (!castlingRights.isRightForColor(onTurn))
					return true;

				// Check if king is in check
				const Square::Type kingPosition = this->position.getKingPosition(onTurn);

				if (this->position.isSquareAttacked(oppositeColor, kingPosition))
					return true;

				// Try both castlings
				move.setMovingPieceType(PieceType::KING);
				move.setBeginSquare(kingPosition);

				for (CastlingType::Type castlingType = CastlingType::FIRST; castlingType < CastlingType::LAST; castlingType++) {
					if (isCastlingPossible(this->position, castlingType)) {
						const Square::Type kingTargetSquare = CastlingConstants::of(onTurn, castlingType).getKingTargetSquare();
						move.finishCastling(kingTargetSquare);

						if (!this->walker.processMove(move))
  							return false;
	 				}
				}

				return true;
			}
    
			/**
			 * This method checks if it is possible to do given castling in given position.
			 * This method does NOT check:
			 * - if the king is attacked (this is common to both castlings so it is faster to check it outside
			 *     of this method)
			 * - if  the king would be attacked after the move
			 * @param position position
			 * @param castlingType type of castling
			 * @return if the castling is possible
			 */
			static bool isCastlingPossible(Pos const & position, const CastlingType::Type castlingType) {
				const CastlingRights castlingRights = position.getCastlingRights();
				const BitBoard::Type occupancy = position.getOccupancy();
				const Color::Type onTurn = position.getOnTurn();

				if (castlingRights.isRight(onTurn, castlingType)) {
					const CastlingConstants &castlingConstants = CastlingConstants::of(onTurn, castlingType);

					if ((occupancy & castlingConstants.getMiddleSquareMask()) == 0) {
						// We should test square that king goes across. This is same square as rook destination square.
						const Square::Type testSquare = castlingConstants.getKingMiddleSquare();
						const Color::Type oppositeColor = Color::getOppositeColor(onTurn);

						if (!position.isSquareAttacked(oppositeColor, testSquare))
							return true;
					}
				}

				return false;
			}
    		
		public:
			/**
			 * Setting to true means that the generator will not generate moves that cannot stop check. 
			 * @param reduce true in case that moves will be reduced in case of check
			 */
			void setReduceMovesInCheck(const bool reduce) {
				reduceMovesInCheck = reduce;
			}
    
			/**
			 * Setting to true means that the generator doesn't have to generate moves that are not checks.
			 * So the generator generates all the checking moves and (possibly) some non-checking ones. 
			 */
			void setGenerateOnlyChecks(const bool onlyChecks) {
				generateOnlyChecks = onlyChecks;
			}

		private:
			/**
			 * Calculates possible target squares of the piece. This mask cannot be used for king moves, castling or en-passant.
			 * If reduceMovesInCheck is false or if there is no check it simply returns mask of squares not occupied by own pieces.
			 * If reduceMovesInCheck is true and there is a single check it returns the square of checking piece and 
			 * squares between this checking piece and our king.
			 * If reduceMovesInCheck is true and there is a double check it returns BitBoard.EMPTY because the king must move.
			 * @return possible target squares
			 */
			BitBoard::Type calculatePossibleTargetSquaresForReductionInCheck() {
				const BitBoard::Type notOwnSquares = ~this->position.getColorOccupancy(onTurn);

				if (!reduceMovesInCheck)
					return notOwnSquares;

				const Square::Type kingSquare = this->position.getKingPosition(onTurn);
				const BitBoard::Type checkingPieces = this->position.getAttackingPieces(oppositeColor, kingSquare);
    	
				if (checkingPieces == BitBoard::EMPTY)
					return notOwnSquares;   // No check
    	
				if (BitBoard::hasSingleSquare(checkingPieces)) {
					const Square::Type checkingSquare = BitBoard::getFirstSquare(checkingPieces);
			
					return checkingPieces | BetweenTable::getItem(kingSquare, checkingSquare);
				}
				else
					return BitBoard::EMPTY;   // Double check => king must move
			}

			void updateCheckingMasksOnlyChecks() {
				// Direct check
				const LineIndexer::IndexType diagonalIndex = LineIndexer::getLineIndex(CrossDirection::DIAGONAL, oppositeKingSquare, this->position.getOccupancy());
				diagonalCheckingMask = LineAttackTable::getAttackMask(diagonalIndex);

				const LineIndexer::IndexType orthogonalIndex = LineIndexer::getLineIndex(CrossDirection::ORTHOGONAL, oppositeKingSquare, this->position.getOccupancy());
				orthogonalCheckingMask = LineAttackTable::getAttackMask(orthogonalIndex);

				// Indirect check
				const BitBoard::Type orthogonalFullMask = FigureAttackTable::getItem(PieceType::ROOK, oppositeKingSquare);
				const BitBoard::Type diagonalFullMask = FigureAttackTable::getItem(PieceType::BISHOP, oppositeKingSquare);
		
				const BitBoard::Type rookMask = this->position.getBothColorPiecesMask(PieceType::ROOK);
				const BitBoard::Type bishopMask = this->position.getBothColorPiecesMask(PieceType::BISHOP);
				const BitBoard::Type queenMask = this->position.getBothColorPiecesMask(PieceType::QUEEN);
				const BitBoard::Type ownPieces = this->position.getColorOccupancy(onTurn);
		
				const BitBoard::Type potentialIndirectMask = ownPieces & (
					(orthogonalFullMask & (rookMask | queenMask)) |
					(diagonalFullMask & (bishopMask | queenMask))
				);
		
				BitBoard::Type blockers = BitBoard::EMPTY;
		
				for (BitLoop loop(potentialIndirectMask); loop.hasNextSquare(); ) {
					const Square::Type square = loop.getNextSquare();
					const BitBoard::Type betweenMask = BetweenTable::getItem(oppositeKingSquare, square) & this->position.getOccupancy();
			
					if (BitBoard::hasSingleSquare(betweenMask))
						blockers |= betweenMask;
				}
		
				indirectCheckingBlockers = blockers & ownPieces;
			}
    
			void updateCheckingMasksAllMoves() {
				diagonalCheckingMask = BitBoard::FULL;
				orthogonalCheckingMask = BitBoard::FULL;
				indirectCheckingBlockers = BitBoard::FULL;
			}

		public:
			/**
			 * Generates all legal moves in the position.
			 * Calls walker for each generated move.
			 */
			void generateMoves() {
				onTurn = this->position.getOnTurn();
				oppositeColor = Color::getOppositeColor(onTurn);
				oppositeKingSquare = this->position.getKingPosition(oppositeColor);

				const CastlingRights::Index castlingRightIndex = this->position.getCastlingRights().getIndex();
				const File::Type epFile = this->position.getEpFile();
				move.initialize(castlingRightIndex, epFile);

				const BitBoard::Type notOwnSquares = ~this->position.getColorOccupancy(onTurn);

				if (generateOnlyChecks)
					updateCheckingMasksOnlyChecks();
				else
					updateCheckingMasksAllMoves();

				// Generate figure moves, starting with king - this speeds up legal move checking
				if (this->getGenerateMovesOfPiece(PieceType::KING)) {
					if (!generateShortMovingFigureMoves(PieceType::KING, notOwnSquares))
						return;
				}

				const BitBoard::Type possibleTargetSquaresForReductionInCheck = calculatePossibleTargetSquaresForReductionInCheck();
    	
				if (this->getGenerateMovesOfPiece(PieceType::KNIGHT)) {
					if (!generateShortMovingFigureMoves(PieceType::KNIGHT, possibleTargetSquaresForReductionInCheck))
						return;
				}
    	
				if (this->getGenerateMovesOfPiece(PieceType::QUEEN)) {
					if (!generateQueenMoves(possibleTargetSquaresForReductionInCheck))
						return;
				}
    	
				if (this->getGenerateMovesOfPiece(PieceType::ROOK)) {
					if (!generateRookMoves(possibleTargetSquaresForReductionInCheck))
						return;
				}
    	
				if (this->getGenerateMovesOfPiece(PieceType::BISHOP)) {
					if (!generateBishopMoves(possibleTargetSquaresForReductionInCheck))
						return;
				}
    	
				if (this->getGenerateMovesOfPiece(PieceType::PAWN)) {
					if (!generatePawnMoves(possibleTargetSquaresForReductionInCheck))
						return;
				}

				if (this->getGenerateMovesOfPiece(PieceType::KING)) {
					if (!generateCastlingMoves())
						return;
				}

				if (this->getGenerateMovesOfPiece(PieceType::PAWN)) {
					this->generateEnPassantMoves();
				}
			}

			PseudoLegalMoveGenerator (Pos & position_, Walker & walker_):
				PseudoLegalMoveGeneratorBase<Pos, Walker>(position_, walker_),
				reduceMovesInCheck(false),
				generateOnlyChecks(false)
			{
			}
	};
}

#endif

