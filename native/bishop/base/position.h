
#ifndef _BISHOP_BASE_POSITION_H_
#define _BISHOP_BASE_POSITION_H_


#include "bit_board.h"
#include "color.h"
#include "castling_rights.h"
#include "file.h"
#include "square.h"
#include "piece_type.h"
#include "move.h"
#include "castling_constants.h"
#include "figure_attack_table.h"
#include "line_indexer.h"
#include "line_attack_table.h"
#include "pawn_attack_table.h"
#include "super_attack_table.h"
#include "bit_loop.h"
#include "piece.h"

#include <stdexcept>


namespace bishop::base {

	/**
	 * Representation of chess position with additional information that affects
	 * generation of moves. 
	 * @author Ing. Petr Ležák
	 */
	template<typename Caching>
	class Position {
		private:
			// Primary piece information - the source of truth
			BitBoard::Type pieceTypeMasks[PieceType::LAST];   // Which squares are occupied by given piece types (no matter of color)
			BitBoard::Type colorOccupancy[Color::LAST];   // Which squares are occupied by some piece with given color
	
			// Cached piece information
			BitBoard::Type occupancy;   // Which squares are occupied by some piece

			Color::Type onTurn;   // Color of player on turn
			CastlingRights castlingRights;   // Rights for castling
			File::Type epFile;   // File where pawn advanced by two squares in last move (or File.NONE)

			Caching caching;

		public:
			/**
			 * Default constructor - creates empty position.
			 */
			Position()//:
				//caching(*this)
			{
				clearPosition();
			}
			
		private:
			/**
			 * Makes given normal move.
			 * @param move move
			 */
			inline void makeNormalMove (const Move move) {
				const PieceType::Type movingPieceType = move.getMovingPieceType();
				assert (PieceType::isValid (movingPieceType));

				const Square::Type beginSquare = move.getBeginSquare();
				assert (Square::isValid (beginSquare));

				const Square::Type targetSquare = move.getTargetSquare();
				assert (Square::isValid (targetSquare));

				const BitBoard::Type beginSquareMask = BitBoard::getSquareMask(beginSquare);
				const BitBoard::Type targetSquareMask = BitBoard::getSquareMask(targetSquare);

				const PieceType::Type capturedPieceType = move.getCapturedPieceType();
				const Color::Type oppositeColor = Color::getOppositeColor (onTurn);
		
				const BitBoard::Type moveMask = beginSquareMask | targetSquareMask;
				
				pieceTypeMasks[movingPieceType] ^= moveMask;
				colorOccupancy[onTurn] ^= moveMask;
				occupancy ^= moveMask;
		
				caching.movePiece (onTurn, movingPieceType, beginSquare, targetSquare);

				if (capturedPieceType != PieceType::NONE) {
					pieceTypeMasks[capturedPieceType] ^= targetSquareMask;
					colorOccupancy[oppositeColor] ^= targetSquareMask;
					occupancy ^= targetSquareMask;
			
					caching.removePiece (oppositeColor, capturedPieceType, targetSquare);
				}

				// Update castling rights
				if (!castlingRights.isEmpty() && (moveMask & CastlingRights::AFFECTED_SQUARES) != 0) {
					const CastlingRights::Index origCastlingRightIndex = castlingRights.getIndex();
			
					castlingRights.updateAfterSquareChange (beginSquare);
					castlingRights.updateAfterSquareChange (targetSquare);
			
					caching.changeCastlingRights (origCastlingRightIndex, castlingRights.getIndex());
				}

				// On turn
				onTurn = oppositeColor;
				caching.swapOnTurn();

				// Update EP file
				const File::Type origEpFile = epFile;
				
				if (movingPieceType == PieceType::PAWN && abs(targetSquare - beginSquare) == 2 * File::LAST) {
					epFile = Square::getFile(beginSquare);   // Needed by method isEnPassantPossible
			
					if (!isEnPassantPossible())
						epFile = File::NONE;
				}
				else
					epFile = File::NONE;
		
				caching.changeEpFile (origEpFile, epFile);
			}

			/**
			 * Undos given normal move.
			 * @param move move
			 */
			inline void undoNormalMove (const Move move) {
				const PieceType::Type movingPieceType = move.getMovingPieceType();
				assert (PieceType::isValid (movingPieceType));

				const Square::Type beginSquare = move.getBeginSquare();
				assert (Square::isValid (beginSquare));

				const Square::Type targetSquare = move.getTargetSquare();
				assert (Square::isValid (targetSquare));

				const BitBoard::Type beginSquareMask = BitBoard::getSquareMask(beginSquare);
				const BitBoard::Type targetSquareMask = BitBoard::getSquareMask(targetSquare);

				const PieceType::Type capturedPieceType = move.getCapturedPieceType();
				const Color::Type oppositeColor = onTurn;
		
				// On turn
				onTurn = Color::getOppositeColor (oppositeColor);
				caching.swapOnTurn();
		
				// Piece masks
				const BitBoard::Type moveMask = beginSquareMask | targetSquareMask;
				
				pieceTypeMasks[movingPieceType] ^= moveMask;
				colorOccupancy[onTurn] ^= moveMask;
				occupancy ^= moveMask;
		
				caching.movePiece(onTurn, movingPieceType, targetSquare, beginSquare);

				if (capturedPieceType != PieceType::NONE) {
					pieceTypeMasks[capturedPieceType] ^= targetSquareMask;
					colorOccupancy[oppositeColor] ^= targetSquareMask;
					occupancy ^= targetSquareMask;
			
					caching.addPiece(oppositeColor, capturedPieceType, targetSquare);
				}

				// Update castling rights
				const CastlingRights::Index prevCastlingRightIndex = move.getPreviousCastlingRigthIndex();
		
				if (prevCastlingRightIndex != castlingRights.getIndex()) {
					caching.changeCastlingRights(castlingRights.getIndex(), prevCastlingRightIndex);
					castlingRights.setIndex (prevCastlingRightIndex);
				}

				// Update EP file
				const File::Type prevEpFile = move.getPreviousEpFile();
				
				if (prevEpFile != epFile) {
					caching.changeEpFile(epFile, prevEpFile);
					epFile = prevEpFile;
				}
			}

			/**
			 * Makes given promotion move.
			 * @param move move
			 */
			inline void makePromotionMove (const Move move) {
				const Square::Type beginSquare = move.getBeginSquare();
				assert (Square::isValid (beginSquare));

				const Square::Type targetSquare = move.getTargetSquare();
				assert (Square::isValid (targetSquare));

				const BitBoard::Type beginSquareMask = BitBoard::getSquareMask(beginSquare);
				const BitBoard::Type targetSquareMask = BitBoard::getSquareMask(targetSquare);

				const PieceType::Type capturedPieceType = move.getCapturedPieceType();
				const PieceType::Type promotionPieceType = move.getPromotionPieceType();
				const Color::Type oppositeColor = Color::getOppositeColor (onTurn);
		
				const BitBoard::Type moveMask = beginSquareMask | targetSquareMask;

				pieceTypeMasks[PieceType::PAWN] ^= beginSquareMask;
				pieceTypeMasks[promotionPieceType] ^= targetSquareMask;
				colorOccupancy[onTurn] ^= moveMask;
				occupancy ^= moveMask;
		
				caching.removePiece(onTurn, PieceType::PAWN, beginSquare);
				caching.addPiece(onTurn, promotionPieceType, targetSquare);
		
				if (capturedPieceType != PieceType::NONE) {
					pieceTypeMasks[capturedPieceType] ^= targetSquareMask;
					colorOccupancy[oppositeColor] ^= targetSquareMask;
					occupancy ^= targetSquareMask;
			
					caching.removePiece(oppositeColor, capturedPieceType, targetSquare);
				}			

				// Update castling rights - only by target square (begin square cannot change rights)
				const CastlingRights::Index origCastlingRightIndex = castlingRights.getIndex();
				castlingRights.updateAfterSquareChange (targetSquare);
				caching.changeCastlingRights(origCastlingRightIndex, castlingRights.getIndex());
		
				caching.changeEpFile(epFile, File::NONE);
				epFile = File::NONE;
		
				onTurn = oppositeColor;
				caching.swapOnTurn();
			}

			/**
			 * Undos given promotion move.
			 * @param move move
			 */
			inline void undoPromotionMove (const Move move) {
				const Square::Type beginSquare = move.getBeginSquare();
				assert (Square::isValid (beginSquare));

				const Square::Type targetSquare = move.getTargetSquare();
				assert (Square::isValid (targetSquare));

				const BitBoard::Type beginSquareMask = BitBoard::getSquareMask(beginSquare);
				const BitBoard::Type targetSquareMask = BitBoard::getSquareMask(targetSquare);

				const PieceType::Type capturedPieceType = move.getCapturedPieceType();
				const PieceType::Type promotionPieceType = move.getPromotionPieceType();
				const Color::Type oppositeColor = onTurn;
		
				// On turn
				onTurn = Color::getOppositeColor (oppositeColor);
				caching.swapOnTurn();

				// Piece masks
				const BitBoard::Type moveMask = beginSquareMask | targetSquareMask;
		
				pieceTypeMasks[PieceType::PAWN] ^= beginSquareMask;
				pieceTypeMasks[promotionPieceType] ^= targetSquareMask;
				colorOccupancy[onTurn] ^= moveMask;
				occupancy ^= moveMask;

				caching.addPiece(onTurn, PieceType::PAWN, beginSquare);
				caching.removePiece(onTurn, promotionPieceType, targetSquare);

				if (capturedPieceType != PieceType::NONE) {
					pieceTypeMasks[capturedPieceType] ^= targetSquareMask;
					colorOccupancy[oppositeColor] ^= targetSquareMask;
					occupancy ^= targetSquareMask;
			
					caching.addPiece(oppositeColor, capturedPieceType, targetSquare);
				}

				// Update castling rights
				const CastlingRights::Index prevCastlingRightIndex = move.getPreviousCastlingRigthIndex();
		
				if (prevCastlingRightIndex != castlingRights.getIndex()) {
					caching.changeCastlingRights(castlingRights.getIndex(), prevCastlingRightIndex);
					castlingRights.setIndex (prevCastlingRightIndex);
				}
		
				// EP file
				epFile = move.getPreviousEpFile();
				caching.changeEpFile(File::NONE, epFile);
			}

			/**
			 * Makes given castling move.
			 * @param move move
			 */
			inline void makeCastlingMove (const Move move) {
				const Square::Type beginSquare = move.getBeginSquare();
				assert (Square::isValid (beginSquare));

				const Square::Type targetSquare = move.getTargetSquare();
				assert (Square::isValid (targetSquare));

				const Color::Type oppositeColor = Color::getOppositeColor (onTurn);

				const CastlingType::Type castlingType = (beginSquare > targetSquare) ? CastlingType::LONG : CastlingType::SHORT;

				CastlingConstants const &castlingConstants = CastlingConstants::of(onTurn, castlingType);

				const BitBoard::Type kingChanges = castlingConstants.getKingChangeMask();
				const BitBoard::Type rookChanges = castlingConstants.getRookChangeMask();
				const BitBoard::Type occupancyChanges = kingChanges ^ rookChanges;

				const Square::Type rookBeginSquare = castlingConstants.getRookBeginSquare();
				const Square::Type rookTargetSquare = castlingConstants.getRookTargetSquare();
		
				caching.movePiece(onTurn, PieceType::KING, beginSquare, targetSquare);
				caching.movePiece(onTurn, PieceType::ROOK, rookBeginSquare, rookTargetSquare);

				pieceTypeMasks[PieceType::KING] ^= kingChanges;
				pieceTypeMasks[PieceType::ROOK] ^= rookChanges;
				colorOccupancy[onTurn] ^= occupancyChanges;
				occupancy ^= occupancyChanges;

				// Castling rights
				const CastlingRights::Index origCastlingRightIndex = castlingRights.getIndex();
				castlingRights.dropRightsForColor (onTurn);
				caching.changeCastlingRights(origCastlingRightIndex, castlingRights.getIndex());

				// EP file
				caching.changeEpFile(epFile, File::NONE);
				epFile = File::NONE;
		
				// On turn
				onTurn = oppositeColor;
				caching.swapOnTurn();
			}

			/**
			 * Undo given castling move.
			 * @param move move
			 */
			inline void undoCastlingMove (const Move move) {
				const Square::Type beginSquare = move.getBeginSquare();
				assert (Square::isValid (beginSquare));

				const Square::Type targetSquare = move.getTargetSquare();
				assert (Square::isValid (targetSquare));
				
				const Color::Type oppositeColor = onTurn;
		
				// On turn
				onTurn = Color::getOppositeColor (oppositeColor);
				caching.swapOnTurn();

				// Piece masks
				const CastlingType::Type castlingType = (beginSquare > targetSquare) ? CastlingType::LONG : CastlingType::SHORT;

				CastlingConstants const &castlingConstants = CastlingConstants::of(onTurn, castlingType);

				const BitBoard::Type kingChanges = castlingConstants.getKingChangeMask();
				const BitBoard::Type rookChanges = castlingConstants.getRookChangeMask();
				const BitBoard::Type occupancyChanges = kingChanges ^ rookChanges;

				const Square::Type rookBeginSquare = castlingConstants.getRookBeginSquare();
				const Square::Type rookTargetSquare = castlingConstants.getRookTargetSquare();
		
				caching.movePiece(onTurn, PieceType::KING, targetSquare, beginSquare);
				caching.movePiece(onTurn, PieceType::ROOK, rookTargetSquare, rookBeginSquare);

				pieceTypeMasks[PieceType::KING] ^= kingChanges;
				pieceTypeMasks[PieceType::ROOK] ^= rookChanges;
				colorOccupancy[onTurn] ^= occupancyChanges;
				occupancy ^= occupancyChanges;

				// Update castling rights
				const CastlingRights::Index prevCastlingRightIndex = move.getPreviousCastlingRigthIndex();
				caching.changeCastlingRights(prevCastlingRightIndex, castlingRights.getIndex());
				castlingRights.setIndex (prevCastlingRightIndex);
		
				// EP file
				epFile = move.getPreviousEpFile();		
				caching.changeEpFile(File::NONE, epFile);
			}

			/**
			 * Makes given en-passant move.
			 * @param move move
			 */
			inline void makeEnPassantMove (const Move move) {
				const Square::Type beginSquare = move.getBeginSquare();
				assert (Square::isValid (beginSquare));

				const Square::Type targetSquare = move.getTargetSquare();
				assert (Square::isValid (targetSquare));

				const BitBoard::Type beginSquareMask = BitBoard::getSquareMask(beginSquare);
				const BitBoard::Type targetSquareMask = BitBoard::getSquareMask(targetSquare);

				const Color::Type oppositeColor = Color::getOppositeColor (onTurn);
				const Square::Type epSquare = Square::onFileRank(epFile, Square::getRank (beginSquare));
				const BitBoard::Type epSquareMask = BitBoard::getSquareMask(epSquare);
		
				const BitBoard::Type moveMask = beginSquareMask | targetSquareMask;
				const BitBoard::Type changeMask = moveMask | epSquareMask;
		
				pieceTypeMasks[PieceType::PAWN] ^= changeMask;
				colorOccupancy[onTurn] ^= moveMask;
				colorOccupancy[oppositeColor] ^= epSquareMask;
				occupancy ^= changeMask;
		
				caching.movePiece(onTurn, PieceType::PAWN, beginSquare, targetSquare);
				caching.removePiece(oppositeColor, PieceType::PAWN, epSquare);
		
				// EP file
				caching.changeEpFile(epFile, File::NONE);
				epFile = File::NONE;
		
				// On turn
				onTurn = oppositeColor;
				caching.swapOnTurn();
			}

			/**
			 * Undos given en-passant move.
			 * @param move move
			 */
			inline void undoEnPassantMove (const Move move) {
				const Square::Type beginSquare = move.getBeginSquare();
				assert (Square::isValid (beginSquare));

				const Square::Type targetSquare = move.getTargetSquare();
				assert (Square::isValid (targetSquare));

				const BitBoard::Type beginSquareMask = BitBoard::getSquareMask(beginSquare);
				const BitBoard::Type targetSquareMask = BitBoard::getSquareMask(targetSquare);
		
				const Color::Type oppositeColor = onTurn;
		
				// On turn
				onTurn = Color::getOppositeColor (oppositeColor);
				caching.swapOnTurn();
		
				// EP file
				epFile = move.getPreviousEpFile();
				caching.changeEpFile(File::NONE, epFile);
		
				const Square::Type epSquare = Square::onFileRank(epFile, Square::getRank (beginSquare));
				const BitBoard::Type epSquareMask = BitBoard::getSquareMask(epSquare);

				// Piece masks
				const BitBoard::Type moveMask = beginSquareMask | targetSquareMask;
				const BitBoard::Type changeMask = moveMask | epSquareMask;
		
				pieceTypeMasks[PieceType::PAWN] ^= changeMask;
				colorOccupancy[onTurn] ^= moveMask;
				colorOccupancy[oppositeColor] ^= epSquareMask;
				occupancy ^= changeMask;
		
				caching.movePiece(onTurn, PieceType::PAWN, targetSquare, beginSquare);
				caching.addPiece(oppositeColor, PieceType::PAWN, epSquare);
			}
	
			/**
			 * Makes given null move.
			 * @param move move
			 */
			inline void makeNullMove (const Move move) {
				// EP file
				caching.changeEpFile(epFile, File::NONE);
				epFile = File::NONE;
		
				// On turn
				onTurn = Color::getOppositeColor(onTurn);
				caching.swapOnTurn();
			}

			/**
			 * Undos given null move.
			 * @param move move
			 */
			inline void undoNullMove (const Move move) {
				// On turn
				onTurn = Color::getOppositeColor (onTurn);
				caching.swapOnTurn();
		
				// EP file
				epFile = move.getPreviousEpFile();
				caching.changeEpFile(File::NONE, epFile);
			}
		public:
			/**
			 * Returns color of player on turn.
			 * @return color of player on turn
			 */
			inline Color::Type getOnTurn() const {
				return onTurn;
			}

			/**
			 * Sets color of player on turn.
			 * @param onTurn color of player on turn
			 */
			void setOnTurn (const Color::Type onTurn_) {
				if (!Color::isValid(onTurn_))
					throw ::std::runtime_error("Invalid color given as onTurn");

				onTurn = onTurn_;
			}

			/**
			 * Returns EP file.
			 * @return file where pawn was advanced by two squares in last move (or File.NONE) 
			 */
			File::Type getEpFile() const {
				return epFile;
			}

			/**
			 * Sets EP file.
			 * @param file file where pawn was advanced by two squares in last move (or File.NONE)
			 */
			void setEpFile (const File::Type file_) {
				if (!File::isValid (file_) && file_ != File::NONE)
					throw ::std::runtime_error("Invalid file given as epFile");

				epFile = file_;
			}

			/**
			 * Returns piece on given square.
			 * If square if empty method returns null.
			 * @param square coordinate of square
			 * @return piece on given square or null
			 */
/*			public Piece getSquareContent (final int square) {
		final long squareMask = BitBoard.getSquareMask(square);

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			if ((colorOccupancy[color] & squareMask) != 0) {
				final int pieceType = getPieceTypeOnSquare(square);
				
				return Piece.withColorAndType(color, pieceType);
			}
		}
		
		return null;
	}*/

			/**
			 * Puts given piece to given square.
			 * @param square target square
			 * @param piece piece or null if square should be empty
			 */
			void setSquareContent (const Square::Type square, const Piece piece) {
				const BitBoard::Type squareMask = BitBoard::getSquareMask(square);
		
				if (!piece.isEmpty()) {
					const Color::Type color = piece.getColor();
					const Color::Type oppositeColor = Color::getOppositeColor(color);
			
					colorOccupancy[color] |= squareMask;
					colorOccupancy[oppositeColor] &= ~squareMask;
					occupancy |= squareMask;
			
					FOR_EACH_PIECE_TYPE(pieceType) {
						if (pieceType == piece.getPieceType())
							pieceTypeMasks[pieceType] |= squareMask;
						else
							pieceTypeMasks[pieceType] &= ~squareMask;
					}
				}
				else {
					FOR_EACH_COLOR(color)
						colorOccupancy[color] &= ~squareMask;
			
					FOR_EACH_PIECE_TYPE(pieceType)
						pieceTypeMasks[pieceType] &= ~squareMask;
			
					occupancy &= ~squareMask;
				}
			}
	
	/**
	 * Puts given piece to given squares.
	 * @param mask target squares
	 * @param piece piece or null if square should be empty
	 */
	/*public void setMoreSquaresContent (final long mask, final Piece piece) {
		for (BitLoop loop = new BitLoop(mask); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			
			setSquareContent(square, piece);
		}
	}*/
	
			/**
			 * Makes the position empty and white on turn.
			 */
			void clearPosition() {
				occupancy = BitBoard::EMPTY;

				const BitBoard::Type empty = BitBoard::EMPTY;
				::std::fill_n(colorOccupancy, Color::LAST, empty);
				::std::fill_n(pieceTypeMasks, PieceType::LAST, empty);

				onTurn = Color::WHITE;
				castlingRights.clearRights();
				epFile = File::NONE;
			}
	
		private:
			void setInitialPiecesForColor (const Color::Type color, const Rank::Type firstRank, const Rank::Type secondRank) {
				// Figures
				setSquareContent(Square::onFileRank(File::FA, firstRank), Piece::withColorAndType(color, PieceType::ROOK));
				setSquareContent(Square::onFileRank(File::FB, firstRank), Piece::withColorAndType(color, PieceType::KNIGHT));
				setSquareContent(Square::onFileRank(File::FC, firstRank), Piece::withColorAndType(color, PieceType::BISHOP));
				setSquareContent(Square::onFileRank(File::FD, firstRank), Piece::withColorAndType(color, PieceType::QUEEN));
				setSquareContent(Square::onFileRank(File::FE, firstRank), Piece::withColorAndType(color, PieceType::KING));
				setSquareContent(Square::onFileRank(File::FF, firstRank), Piece::withColorAndType(color, PieceType::BISHOP));
				setSquareContent(Square::onFileRank(File::FG, firstRank), Piece::withColorAndType(color, PieceType::KNIGHT));
				setSquareContent(Square::onFileRank(File::FH, firstRank), Piece::withColorAndType(color, PieceType::ROOK));
		
				// Pawns
				const Piece pawn = Piece::withColorAndType(color, PieceType::PAWN);
		
				for (File::Type file = File::FIRST; file < File::LAST; file++)
					setSquareContent(Square::onFileRank(file, secondRank), pawn);
			}
	
		public:
			void setInitialPosition() {
				clearPosition(); 
		
				onTurn = Color::WHITE;
				castlingRights.setFullRights();
				epFile = File::NONE;
		
				setInitialPiecesForColor(Color::WHITE, Rank::R1, Rank::R2);
				setInitialPiecesForColor(Color::BLACK, Rank::R8, Rank::R7);
		
				refreshCachedData();
			}

			/**
			 * Returns mask of squares occupied by given piece.
			 * @param color color of piece
			 * @param type type of piece
			 * @return mask of squares
			 */
			BitBoard::Type getPiecesMask (const Color::Type color, const PieceType::Type type) const {
				return pieceTypeMasks[type] & colorOccupancy[color];
			}

			/**
			 * Returns mask of given piece regardless of their color.
			 * @param type piece type
			 * @return mask of given piece type
			 */
			BitBoard::Type getBothColorPiecesMask(const PieceType::Type type) const {
				return pieceTypeMasks[type];
			}

			/**
			 * Returns mask of promotion figures with given color.
			 * @param color color of figures
			 * @return mask of promotion figures with given color
			 */
			BitBoard::Type getPromotionFigureMask(const Color::Type color) const {
				return colorOccupancy[color] & ~(pieceTypeMasks[PieceType::KING] | pieceTypeMasks[PieceType::PAWN]);
			}
	
			/**
			 * Returns mask of all occupied squares.
			 * @return mask of squares where is some piece 
			 */
			BitBoard::Type getOccupancy() const {
				return occupancy;
			}

			/**
			 * Returns mask of squares occupied by some piece with given color.
			 * @param color color of piece
			 * @return mask of squares occupied by some piece with given color
			 */
			BitBoard::Type getColorOccupancy (const Color::Type color) const {
				return colorOccupancy[color];
			}

			/**
			 * Returns castling rights.
			 * @return castling rights, should not be modified
			 */
			CastlingRights getCastlingRights() const {
				return castlingRights;
			}

			/**
			 * Sets castling rights.
			 * @param rights
			 */
			void setCastlingRights (const CastlingRights rights_) {
				castlingRights = rights_;
			}

			/**
			 * Checks if given square is attacked by some piece with given color.
			 * @param color color of attacking pieces
			 * @param square checked square
			 * @return true if square is attacked, false if not
			 */
			bool isSquareAttacked (const Color::Type color, const Square::Type square) const {
				// Speedup (especially in endings) - if there is no piece by color occupancy that
				// can attack the square then return false.
				if ((getColorOccupancy(color) & SuperAttackTable::getItem(square)) == 0)
					return false;

				// Short move figures
				if ((getPiecesMask(color, PieceType::KING) & FigureAttackTable::getItem (PieceType::KING, square)) != 0)
					return true;

				if ((getPiecesMask(color, PieceType::KNIGHT) & FigureAttackTable::getItem (PieceType::KNIGHT, square)) != 0)
					return true;

				// Pawn
				const Color::Type oppositeColor = Color::getOppositeColor(color);

				if ((getPiecesMask(color, PieceType::PAWN) & PawnAttackTable::getItem(oppositeColor, square)) != 0)
					return true;

				// Long move figures
				const LineIndexer::IndexType orthogonalIndex = LineIndexer::getLineIndex(CrossDirection::ORTHOGONAL, square, occupancy);
				const BitBoard::Type orthogonalMask = LineAttackTable::getAttackMask(orthogonalIndex);
		
				if ((getPiecesMask(color, PieceType::ROOK) & orthogonalMask) != 0)
					return true;

				const LineIndexer::IndexType diagonalIndex = LineIndexer::getLineIndex(CrossDirection::DIAGONAL, square, occupancy);
				const BitBoard::Type diagonalMask = LineAttackTable::getAttackMask(diagonalIndex);

				if ((getPiecesMask(color, PieceType::BISHOP) & diagonalMask) != 0)
					return true;
		
				const BitBoard::Type queenMask = orthogonalMask | diagonalMask;
		
				if ((getPiecesMask(color, PieceType::QUEEN) & queenMask) != 0)
					return true;
				
				return false;
			}

			/**
			 * Returns mask of pieces with given color that attacks given square.
			 * @param color color of attacking pieces
			 * @param square checked square
			 * @return mask of attacking pieces
			 */
			BitBoard::Type getAttackingPieces (const Color::Type color, const Square::Type square) const {
				// Speedup (especially in endings) - if there is no piece by color occupancy that
				// can attack the square then return false.
				if ((getColorOccupancy(color) & SuperAttackTable::getItem(square)) == 0)
					return BitBoard::EMPTY;

				BitBoard::Type attackingPieceMask = BitBoard::EMPTY;
		
				// Short move figures
				attackingPieceMask |= pieceTypeMasks[PieceType::KING] & FigureAttackTable::getItem (PieceType::KING, square);
				attackingPieceMask |= pieceTypeMasks[PieceType::KNIGHT] & FigureAttackTable::getItem (PieceType::KNIGHT, square);

				// Pawn
				const Color::Type oppositeColor = Color::getOppositeColor(color);
				attackingPieceMask |= pieceTypeMasks[PieceType::PAWN] & PawnAttackTable::getItem(oppositeColor, square);

				// Long move figures
				const LineIndexer::IndexType orthogonalIndex = LineIndexer::getLineIndex(CrossDirection::ORTHOGONAL, square, occupancy);
				const BitBoard::Type orthogonalMask = LineAttackTable::getAttackMask(orthogonalIndex);
		
				attackingPieceMask |= pieceTypeMasks[PieceType::ROOK] & orthogonalMask;

				const LineIndexer::IndexType diagonalIndex = LineIndexer::getLineIndex(CrossDirection::DIAGONAL, square, occupancy);
				const BitBoard::Type diagonalMask = LineAttackTable::getAttackMask(diagonalIndex);

				attackingPieceMask |= pieceTypeMasks[PieceType::BISHOP] & diagonalMask;
		
				const BitBoard::Type queenMask = orthogonalMask | diagonalMask;
				attackingPieceMask |= pieceTypeMasks[PieceType::QUEEN] & queenMask;

				return attackingPieceMask & colorOccupancy[color];
			}
	
			/**
			 * Returns count of pieces with given color that attacks given square.
			 * @param color color of attacking pieces
			 * @param square checked square
			 * @return count of attacking pieces
			 */
			int getCountOfAttacks (const Color::Type color, const Square::Type square) const {
				const BitBoard::Type attackingPieceMask = getAttackingPieces(color, square);
				
				return BitBoard::getSquareCountSparse(attackingPieceMask);
			}

			/**
			 * Finds type of piece on given square and returns it.
			 * @param square searched square
			 * @return type of piece on given square or PieceType.NONE
			 */
			int getPieceTypeOnSquare (const Square::Type square) const {
				const BitBoard::Type mask = BitBoard::getSquareMask(square);
		
				if ((occupancy & mask) == 0)
					return PieceType::NONE;

				if ((pieceTypeMasks[PieceType::PAWN] & mask) != 0)
					return PieceType::PAWN;

				if ((pieceTypeMasks[PieceType::KNIGHT] & mask) != 0)
					return PieceType::KNIGHT;

				if ((pieceTypeMasks[PieceType::BISHOP] & mask) != 0)
					return PieceType::BISHOP;

				if ((pieceTypeMasks[PieceType::ROOK] & mask) != 0)
					return PieceType::ROOK;

				if ((pieceTypeMasks[PieceType::QUEEN] & mask) != 0)
					return PieceType::QUEEN;
		
				if ((pieceTypeMasks[PieceType::KING] & mask) != 0)
					return PieceType::KING;

				throw ::std::runtime_error("Corrupted position");
			}

			/**
			 * Makes given move.
			 * @param move move
			 */
			void makeMove (const Move move) {
				switch (move.getMoveType()) {
					case MoveType::NORMAL:
						makeNormalMove (move);
						break;
	
					case MoveType::PROMOTION:
						makePromotionMove (move);
						break;
	
					case MoveType::CASTLING:
						makeCastlingMove (move);
						break;
	
					case MoveType::EN_PASSANT:
						makeEnPassantMove (move);
						break;
				
					case MoveType::NULL_MOVE:
						makeNullMove (move);
						break;

					default:
						// Bad move type
						throw ::std::runtime_error("Bad type of move");
				}
			}

			/**
			 * Undos given move.
			 * @param move move
			 */
			void undoMove (const Move move) {
				switch (move.getMoveType()) {
					case MoveType::NORMAL:
						undoNormalMove (move);
						break;
	
					case MoveType::PROMOTION:
						undoPromotionMove (move);
						break;
			
					case MoveType::CASTLING:
						undoCastlingMove (move);
						break;
	
					case MoveType::EN_PASSANT:
						undoEnPassantMove (move);
						break;

					case MoveType::NULL_MOVE:
						undoNullMove (move);
						break;

					default:
						// Bad move type
						throw ::std::runtime_error("Bad type of move");
				}
			}
	
			/**
			 * Returns position of king with given color.
			 * @param color color of king
			 * @return square where is king with given color
			 */
			Square::Type getKingPosition (const Color::Type color) const {
				const BitBoard::Type kingMask = getPiecesMask(color, PieceType::KING);
				
				return BitBoard::getFirstSquare(kingMask);
			}

			/**
			 * This method updates cached data.
			 * It must be called after manual changing of position (other than by makeMove).
			 */
			void refreshCachedData() {
				updatePiecesMasks();
				updateCaches();
			}

		private:
			void updatePiecesMasks() {
				occupancy = BitBoard::EMPTY;
		
				for (Color::Type color = Color::FIRST; color < Color::LAST; color++)
					occupancy |= colorOccupancy[color];
			}
	
/*	public long calculateHash() {
		long hash = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				for (BitLoop loop = new BitLoop(getPiecesMask(color, pieceType)); loop.hasNextSquare(); ) {
					final int square = loop.getNextSquare();
					
					hash ^= PieceHashTable.getItem(color, pieceType, square);					
				}
			}
		}
		
		hash ^= HashConstants.getOnTurnHash(onTurn);
		hash ^= HashConstants.getEpFileHash(epFile);
		hash ^= HashConstants.getCastlingRightHash(castlingRights.getIndex());
		
		return hash;
	}*/
	
			void updateCaches() {
				//caching.refreshCache(*this);
			}

		public:
			/**
			 * Decides if there is check in the position.
			 * @return true if there is check, false if not
			 */
			bool isCheck() const {
				const Square::Type kingPosition = getKingPosition(onTurn);

				return isSquareAttacked(Color::getOppositeColor(onTurn), kingPosition);
			}

	/**
	 * Returns hash code for use by collections.
	 */
	/*public int hashCode() {
		return (int) getHash();
	}*/

	/**
	 * Returns table position evaluation for given game stage.
	 * @param gameStage game stage
	 * @return evaluation from the white side
	 */
/*	public int getTablePositionEvaluation (final int gameStage) {
		return caching.getTablePositionEvaluation(gameStage);
	}*/
	
	/**
	 * Checks integrity of the position.
	 * This method may be called after refreshCachedData and some calls to makeMove. It checks
	 * if cached data and some another data are still correct. If not it throws an exception.
	 */
			void checkIntegrity() {
				// Piece masks
				BitBoard::Type expectedOccupancy = 0;
		
				FOR_EACH_COLOR(color) {
					BitBoard::Type expectedColorOccupancy = 0;
					
					FOR_EACH_PIECE_TYPE(pieceType) {
						const BitBoard::Type pieceMask = getPiecesMask(color, pieceType);
				
						if ((pieceMask & expectedOccupancy) != 0)
							throw ::std::runtime_error("There are more pieces on one square");
				
						expectedOccupancy |= pieceMask;
						expectedColorOccupancy |= pieceMask;
					}
			
					if (colorOccupancy[color] != expectedColorOccupancy)
						throw ::std::runtime_error("Corrupted color occupancy");
				}
		
				if (this.occupancy != expectedOccupancy)
					throw ::std::runtime_error("Corrupted occupancy");
		
				FOR_EACH_SQUARE(square) {
					const BitBoard::Type squareMask = BitBoard::getSquareMask(square);
					const PieceType::Type pieceType = getPieceTypeOnSquare(square);
			
					if (pieceType == PieceType::NONE) {
						if ((occupancy & squareMask) != 0)
							throw ::std::runtime_error("Corrupted NONE pieceType");
					}
					else {
						const BitBoard::Type whiteMask = getPiecesMask(Color::WHITE, pieceType);
						const BitBoard::Type blackMask = getPiecesMask(Color::BLACK, pieceType);
				
						if (((whiteMask | blackMask) & squareMask) == 0)
							throw ::std::runtime_error("Corrupted pieceType");
					}
				}
		
				// Hash
				/*
				final long oldHash = getHash();
				final MaterialHash oldMaterialHash = getMaterialHash().copy();
				final long oldCombinedEvaluation = caching.getCombinedEvaluation();
				final int oldMaterialEvaluation = caching.getMaterialEvaluation();
				final int oldGameStageUnbound = caching.getGameStageUnbound();

				updateCaches();
		
				if (getHash() != oldHash)
					throw new RuntimeException("Hash was corrupted");

				if (!getMaterialHash().equals(oldMaterialHash))
					throw new RuntimeException("Material hash was corrupted");

				if (caching.getCombinedEvaluation() != oldCombinedEvaluation)
					throw new RuntimeException("Combined evaluation was corrupted");

				if (caching.getMaterialEvaluation() != oldMaterialEvaluation)
					throw new RuntimeException("Material evaluation was corrupted");

				if (caching.getGameStageUnbound() != oldGameStageUnbound)
					throw new RuntimeException("Game stage unbound was corrupted");*/
			}
	
	/**
	 * Returns hash of this position.
	 * @return position hash
	 */
/*	public long getHash() {
		return caching.getHash();
	}*/

	/**
	 * Returns color of side with more pieces.
	 * @return color of side with more pieces
	 */
	/*public int getSideWithMorePieces() {
		final int whitePieceCount = BitBoard.getSquareCount(colorOccupancy[Color.WHITE]);
		final int blackPieceCount = BitBoard.getSquareCount(colorOccupancy[Color.BLACK]);
		
		return (whitePieceCount >= blackPieceCount) ? Color.WHITE : Color.BLACK;
	}*/

			/**
			 * Checks if EP is possible.
			 * @return true if EP is possible, false if not
			 */
		   	bool isEnPassantPossible() {
				if (epFile == File::NONE)
    					return false;

				const Color::Type oppositeColor = Color::getOppositeColor(onTurn);
		 	   	const Square::Type epSquare = BoardConstants::getEpSquare(oppositeColor, epFile);
				const BitBoard::Type possibleBeginSquares = BoardConstants::getConnectedPawnSquareMask(epSquare) & getPiecesMask(onTurn, PieceType::PAWN);

				for (BitLoop loop(possibleBeginSquares); loop.hasNextSquare(); ) {
					const Square::Type beginSquare = loop.getNextSquare();
					Move epCheckMove;

					epCheckMove.initialize(CastlingRights::FIRST_INDEX, epFile);
					epCheckMove.setMovingPieceType(PieceType::PAWN);
					epCheckMove.setBeginSquare (beginSquare);
					epCheckMove.finishEnPassant (BoardConstants::getEpTargetSquare(oppositeColor, epFile));

					makeEnPassantMove(epCheckMove);

					const bool isCheck = isKingNotOnTurnAttacked();

					undoEnPassantMove(epCheckMove);

					if (!isCheck)
						return true;
				}

		    	return false;
		    }
/*
	public IMaterialHashRead getMaterialHash() {
    	return caching.getMaterialHash();
    }
    
	public MaterialHash calculateMaterialHash() {
		return new MaterialHash (this, onTurn);
	}
	
	public String toString() {
		final Fen fen = new Fen();
		fen.setPosition(this);
		
		final StringWriter writer = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(writer);
		fen.writeFen(printWriter);
		printWriter.flush();
		
		return writer.toString();
	}*/
	
			/**
			 * Checks if given pseudolegal move is legal.
			 * Temporary modifies this position.
			 * @param move move to check
			 * @return if move is legal
			 */
			bool isLegalMove(const Move move) {
				makeMove(move);

				const bool isAttack = isKingNotOnTurnAttacked();
		
				undoMove(move);
			
				return !isAttack;
			}

			bool isKingNotOnTurnAttacked() const {
				const Color::Type oppositeColor = Color::getOppositeColor(onTurn);
				const Square::Type kingPos = getKingPosition(oppositeColor);

				return isSquareAttacked(onTurn, kingPos);
			}
	/*
	public int getCheapestAttacker (final int color, final int square, final long effectiveOccupancy) {
		// Pawn
		final int oppositeColor = Color.getOppositeColor(color);
		final long attackingPawnMask = getPiecesMask(color, PieceType.PAWN) & PawnAttackTable.getItem(oppositeColor, square) & effectiveOccupancy;

		if (attackingPawnMask != 0)
			return BitBoard.getFirstSquare(attackingPawnMask);

		// Knight
		final long attackingKnightMask = getPiecesMask(color, PieceType.KNIGHT) & FigureAttackTable.getItem (PieceType.KNIGHT, square) & effectiveOccupancy;
		
		if (attackingKnightMask != 0)
			return BitBoard.getFirstSquare(attackingKnightMask);
		
		// Bishop
		final long attackingBishopMask = getPiecesMask(color, PieceType.BISHOP) & FigureAttackTable.getItem(PieceType.BISHOP, square) & effectiveOccupancy;
		
		for (BitLoop loop = new BitLoop(attackingBishopMask); loop.hasNextSquare(); ) {
			final int testSquare = loop.getNextSquare();

			if ((BetweenTable.getItem(square, testSquare) & effectiveOccupancy) == 0)
				return testSquare;
		}
		
		// Rook
		final long attackingRookMask = getPiecesMask(color, PieceType.ROOK) & FigureAttackTable.getItem(PieceType.ROOK, square) & effectiveOccupancy;
		
		for (BitLoop loop = new BitLoop(attackingRookMask); loop.hasNextSquare(); ) {
			final int testSquare = loop.getNextSquare();

			if ((BetweenTable.getItem(square, testSquare) & effectiveOccupancy) == 0)
				return testSquare;
		}

		// Queen
		final long attackingQueenMask = getPiecesMask(color, PieceType.QUEEN) & FigureAttackTable.getItem(PieceType.QUEEN, square) & effectiveOccupancy;
		
		for (BitLoop loop = new BitLoop(attackingQueenMask); loop.hasNextSquare(); ) {
			final int testSquare = loop.getNextSquare();

			if ((BetweenTable.getItem(square, testSquare) & effectiveOccupancy) == 0)
				return testSquare;
		}

		// King
		final long attackingKingMask = getPiecesMask(color, PieceType.KING) & FigureAttackTable.getItem (PieceType.KING, square) & effectiveOccupancy;
				
		if (attackingKingMask != 0)
			return BitBoard.getFirstSquare(attackingKingMask);
		
		return Square.NONE;
	}

	public int getStaticExchangeEvaluation (final int color, final int square, final PieceTypeEvaluations pieceTypeEvaluations) {
		return getStaticExchangeEvaluation(color, square, pieceTypeEvaluations, occupancy, getPieceTypeOnSquare(square));
	}

	public int getStaticExchangeEvaluation (final int color, final Move move, final PieceTypeEvaluations pieceTypeEvaluations) {
		final int movingPieceType = move.getMovingPieceType();
		final int capturedPieceType = move.getCapturedPieceType();

		return pieceTypeEvaluations.getPieceTypeEvaluation(capturedPieceType) - getStaticExchangeEvaluation(
				Color.getOppositeColor(color),
				move.getTargetSquare(),
				pieceTypeEvaluations,
				occupancy & ~BitBoard.of(move.getBeginSquare()),
				movingPieceType
		);
	}

	private int getStaticExchangeEvaluation (final int color, final int square, final PieceTypeEvaluations pieceTypeEvaluations, final long effectiveOccupancy, final int pieceTypeOnSquare) {
		final int attackerSquare = getCheapestAttacker (color, square, effectiveOccupancy);
		
		if (attackerSquare != Square.NONE) {
			if (pieceTypeOnSquare == PieceType.NONE)
				return 0;
			
			if (pieceTypeOnSquare == PieceType.KING)
				return Evaluation.MAX;
			
			final int attackerPieceType = getPieceTypeOnSquare(attackerSquare);
			final int childEvaluation = getStaticExchangeEvaluation (Color.getOppositeColor(color), square, pieceTypeEvaluations, effectiveOccupancy & ~BitBoard.of(attackerSquare), attackerPieceType);

			return Math.max(0, pieceTypeEvaluations.getPieceTypeEvaluation(pieceTypeOnSquare) - childEvaluation);
		}
		else
			return 0;
	}

	public int getStaticExchangeEvaluationOnTurn(final PieceTypeEvaluations pieceTypeEvaluations) {
		final int notOnTurn = Color.getOppositeColor(onTurn);
		int evaluation = 0;

		for (BitLoop loop = new BitLoop(getColorOccupancy(notOnTurn)); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();

			evaluation = Math.max(evaluation, getStaticExchangeEvaluation(onTurn, square, pieceTypeEvaluations));
		}

		return evaluation;
	}*/

			int getPieceCount(const Color::Type color, const PieceType::Type pieceType) {
				const BitBoard::Type pieceMask = getPiecesMask(color, pieceType);
		
				return BitBoard::getSquareCount(pieceMask);
			}

/*	@Override
	public void setCombinedPositionEvaluationTable(final CombinedPositionEvaluationTable table) {
		caching.setCombinedPositionEvaluationTable(table);
	}

	@Override
	public void setPieceTypeEvaluations (final PieceTypeEvaluations pieceTypeEvaluations) {
		caching.setPieceTypeEvaluations (pieceTypeEvaluations);
	}

	@Override
	public int getMaterialEvaluation() {
		return caching.getMaterialEvaluation();
	}

	@Override
	public int getGameStage() {
		return Math.min(caching.getGameStageUnbound(), GameStage.MAX);
	}*/
	};
}

#endif

