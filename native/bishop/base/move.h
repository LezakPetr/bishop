

#ifndef _BISHOP_BASE_MOVE_H_
#define _BISHOP_BASE_MOVE_H_


namespace bishop::base {
	
	class Move {
		public:
			typedef u_int16_t CompressedMove;

		private:
			unsigned int beginSquare: Square::BIT_COUNT;
			unsigned int targetSquare: Square::BIT_COUNT;
			unsigned int promotionPieceType: PieceType::BIT_COUNT;
			unsigned int movingPieceTyype: PieceType::BIT_COUNT;
			unsigned int capturedPieceTyype: PieceType::BIT_COUNT;
			unsigned int moveType: MoveType::BIT_COUNT;
			unsigned int previousCastlingRight: BIT_COUNT;
			unsigned int previousEpFile: 4;

		public:
			static constexpr CompressedMove NONE_COMPRESSED_MOVE = 0;
	
			static constexpr CompressedMove FIRST_COMPRESSED_MOVE = 0;
			static constexpr CompressedMove LAST_COMPRESSED_MOVE = COMPRESSED_MOVE_MASK + 1;
	
			/**
			 * Clears the move.
			 */
			inline void clear() {
				*this = Move();
			}
    
    			inline void initialize(const int previousCastlingRights_, const File::Type previousEpFile_) {
				previousCastlingRights = previousCastlingRights_;
				previousEpFile = previousEpFile_;
			}

			inline void setMovingPieceType(const PieceType::Type movingPieceType_) {
				movingPieceType = movingPieceType_;
			}

			inline void setBeginSquare (const Square::Type beginSquare_) {
				beginSquare = beginSquare_;
			}

			/**
			 * Sets data of this move.
			 * @param moveType type of move
			 * @param targetSquare target square
			 * @param capturedPieceType type of captured piece
			 * @param promotionPieceType type of promotion piece
			 */
			inline void finishData (const MoveType::Type moveType_, const Square::Type targetSquare_, const PieceType::Type capturedPieceType_, const PieceType::Type promotionPieceType_) {
				moveType = moveType_;
				targetSquare = targetSquare_;
				capturedPieceType = capturedPieceType_;
				promotionPieceType = promotionPieceType_;
			}

			/**
			 * Creates normal move.
			 * @param targetSquare target square
			 * @param capturedPieceType type of captured piece
			 */
			inline void finishNormalMove (const Square::Type targetSquare_, const PieceType::Type capturedPieceType_) {
				finishData (MoveType::NORMAL, targetSquare_, capturedPieceType_, PieceType::NONE);
			}

			/**
			 * Creates promotion of piece.
			 * @param targetSquare target square
			 * @param capturedPieceType type of captured piece
			 * @param promotionPieceType type of promotion piece
			 */
			inline void finishPromotion (const Square::Type targetSquare_, const PieceType::Type capturedPieceType_, const PieceType::Type promotionPieceType_) {
				finishData (MoveType::PROMOTION, targetSquare_, capturedPieceType_, promotionPieceType_);
			}

			/**
			 * Creates castling.
			 * @param targetSquare target square
			 */
			inline void finishCastling (const Square::Type targetSquare_) {
				finishData (MoveType::CASTLING, targetSquare_, PieceType::NONE, PieceType::NONE);
			}

			/**
			 * Creates en-passant move.
			 * @param targetSquare target square
			 */
			inline void finishEnPassant (const Square::Type targetSquare_) {
				finishData (MoveType::EN_PASSANT, targetSquare_, PieceType::PAWN, PieceType::NONE);
			}

			/**
			 * Creates null move.
			 * @param previousCastlingRights castling rights before the move
			 * @param previousEpFile EP file before the move
			 */
			inline void createNull (const int previousCastlingRights_, const File::Type previousEpFile_) {
				initialize(previousCastlingRights_, previousEpFile_);
				setMovingPieceType(PieceType::NONE);
				setBeginSquare (Square::FIRST);
				finishData (MoveType::NULL, Square::FIRST, PieceType::NONE, PieceType::NONE);
			}

			/**
			 * Returns type of move.
			 * @return type of move
			 */
			inline MoveType::Type getMoveType() {
				return moveType;
			}

			/**
			 * Returns type of piece that is moving.
			 * @return moving piece type
			 */
			inline PieceType::Type getMovingPieceType() {
				return movingPieceType;
			}

			/**
			 * Returns begin square of move.
			 * @return begin square of move
			 */
			inline Square::Type getBeginSquare() {
				return beginSquare;
			}

			/**
			 * Returns target square of move.
			 * @return target square of move
			 */
			inline Square::Type getTargetSquare() {
				return targetSquare;
			}

			/**
			 * Returns type of captured piece.
			 * @return type of captured piece
			 */
			inline PieceType::Type getCapturedPieceType() {
				return capturedPieceType;
			}

			/**
			 * Returns type of piece after pawn promotion.
			 * @return type of promotion piece
			 */
			inline PieceType::Type getPromotionPieceType() {
				return promotionPieceType;
			}

			/**
			 * Returns castling right index before the move.
			 * @return castling right index before the move
			 */
			inline int getPreviousCastlingRigthIndex() {
				return previousCastlingRightIndex;
			}

			/**
			 * Returns EP file before the move.
			 * @return EP file before the move
			 */
			inline File::Type getPreviousEpFile() {
				return previousEpFile;
			}

 			static inline bool isPromotion(const PieceType::Type movingPieceType, const Square::Type targetSquare) {
				return movingPieceType == PieceType::PAWN && BitBoard::containsSquare(BoardConstants::RANK_18_MASK, targetSquare);
			}
	};
}


#endif

