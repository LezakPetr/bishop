package bishop.base;

import java.io.PrintWriter;
import java.io.StringWriter;

import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.PawnMoveTable;

public final class Move implements Comparable<Move> {

	private static final int BEGIN_SQUARE_SHIFT                  = 0;
	private static final int TARGET_SQUARE_SHIFT                 = 6;
	private static final int PROMOTION_PIECE_TYPE_SHIFT          = 12;
	private static final int MOVING_PIECE_TYPE_SHIFT             = 15;
	private static final int CAPTURED_PIECE_TYPE_SHIFT           = 18;
	private static final int MOVE_TYPE_SHIFT                     = 21;
	private static final int PREVIOUS_CASTLING_RIGHT_INDEX_SHIFT = 24;
	private static final int PREVIOUS_EP_FILE_SHIFT              = 28;
	
	
	private static final int BEGIN_SQUARE_MASK                  = 0x0000003F;
	private static final int TARGET_SQUARE_MASK                 = 0x00000FC0;
	private static final int PROMOTION_PIECE_TYPE_MASK          = 0x00007000;
	private static final int MOVING_PIECE_TYPE_MASK             = 0x00038000;
	private static final int CAPTURED_PIECE_TYPE_MASK           = 0x001C0000;
	private static final int MOVE_TYPE_MASK                     = 0x00E00000;
	private static final int PREVIOUS_CASTLING_RIGHT_INDEX_MASK = 0x0F000000;
	private static final int PREVIOUS_EP_FILE_MASK              = 0xF0000000;
	
	private static final int FINISH_MASK = TARGET_SQUARE_MASK | PROMOTION_PIECE_TYPE_MASK | CAPTURED_PIECE_TYPE_MASK | MOVE_TYPE_MASK;
	private static final int NOT_FINISH_MASK = ~FINISH_MASK;
	
	
	public static final int COMPRESSED_MOVE_MASK = BEGIN_SQUARE_MASK | TARGET_SQUARE_MASK | PROMOTION_PIECE_TYPE_MASK;
	
	public static final int NONE_COMPRESSED_MOVE = 0;
	
	public static final int FIRST_COMPRESSED_MOVE = 0;
	public static final int LAST_COMPRESSED_MOVE = COMPRESSED_MOVE_MASK + 1;
	
	
	private int data;

    /**
     * Default constructor.
     */
    public Move() {
    	clear();
    }

    /**
     * Clears the move.
     */
    public void clear() {
    	data = 0;
    }
    
    public void initialize(final int previousCastlingRights, final int previousEpFile) {
        data = (previousCastlingRights << PREVIOUS_CASTLING_RIGHT_INDEX_SHIFT) |
        		(previousEpFile << PREVIOUS_EP_FILE_SHIFT);
    }
    
    public void setMovingPieceType(final int movingPieceType) {
    	data &= ~MOVING_PIECE_TYPE_MASK;
    	data |= movingPieceType << MOVING_PIECE_TYPE_SHIFT;
    }
    
    public void setBeginSquare (final int beginSquare) {
    	data &= ~BEGIN_SQUARE_MASK;
    	data |= beginSquare << BEGIN_SQUARE_SHIFT;
    }
    
    /**
     * Sets data of this move.
     * @param moveType type of move
     * @param targetSquare target square
     * @param capturedPieceType type of captured piece
     * @param promotionPieceType type of promotion piece
     */
    private void finishData (final int moveType, final int targetSquare, final int capturedPieceType, final int promotionPieceType) {
    	data &= NOT_FINISH_MASK;
    	
    	data |= (moveType << MOVE_TYPE_SHIFT) |
	            (targetSquare << TARGET_SQUARE_SHIFT) |
	            (capturedPieceType << CAPTURED_PIECE_TYPE_SHIFT) |
	            (promotionPieceType << PROMOTION_PIECE_TYPE_SHIFT);
    }

    /**
     * Creates normal move.
     * @param targetSquare target square
     * @param capturedPieceType type of captured piece
     */
    public void finishNormalMove (final int targetSquare, final int capturedPieceType) {
    	finishData (MoveType.NORMAL, targetSquare, capturedPieceType, PieceType.NONE);
    }

    /**
     * Creates promotion of piece.
     * @param targetSquare target square
     * @param capturedPieceType type of captured piece
     * @param promotionPieceType type of promotion piece
     */
    public void finishPromotion (final int targetSquare, final int capturedPieceType, final int promotionPieceType) {
    	finishData (MoveType.PROMOTION, targetSquare, capturedPieceType, promotionPieceType);
    }

    /**
     * Creates castling.
     * @param targetSquare target square
     */
    public void finishCastling (final int targetSquare) {
    	finishData (MoveType.CASTLING, targetSquare, PieceType.NONE, PieceType.NONE);
    }

    /**
     * Creates en-passant move.
     * @param targetSquare target square
     */
    public void finishEnPassant (final int targetSquare) {
    	finishData (MoveType.EN_PASSANT, targetSquare, PieceType.PAWN, PieceType.NONE);
    }

    /**
     * Creates null move.
     * @param previousCastlingRights castling rights before the move
     * @param previousEpFile EP file before the move
     */
    public void createNull (final int previousCastlingRights, final int previousEpFile) {
    	initialize(previousCastlingRights, previousEpFile);
    	setMovingPieceType(PieceType.NONE);
    	setBeginSquare (Square.FIRST);
    	finishData (MoveType.NULL, Square.FIRST, PieceType.NONE, PieceType.NONE);
    }

    /**
     * Returns type of move.
     * @return type of move
     */
    public int getMoveType() {
    	return (data & MOVE_TYPE_MASK) >>> MOVE_TYPE_SHIFT;
    }

    /**
     * Returns type of piece that is moving.
     * @return moving piece type
     */
    public int getMovingPieceType() {
    	return (data & MOVING_PIECE_TYPE_MASK) >>> MOVING_PIECE_TYPE_SHIFT;
    }

    /**
     * Returns begin square of move.
     * @return begin square of move
     */
    public int getBeginSquare() {
    	return (data & BEGIN_SQUARE_MASK) >>> BEGIN_SQUARE_SHIFT;
    }

    /**
     * Returns target square of move.
     * @return target square of move
     */
    public int getTargetSquare() {
    	return (data & TARGET_SQUARE_MASK) >>> TARGET_SQUARE_SHIFT;
    }

    /**
     * Returns type of captured piece.
     * @return type of captured piece
     */
    public int getCapturedPieceType() {
    	return (data & CAPTURED_PIECE_TYPE_MASK) >>> CAPTURED_PIECE_TYPE_SHIFT;
    }

    /**
     * Returns type of piece after pawn promotion.
     * @return type of promotion piece
     */
    public int getPromotionPieceType() {
    	return (data & PROMOTION_PIECE_TYPE_MASK) >>> PROMOTION_PIECE_TYPE_SHIFT;
    }

    /**
     * Returns castling right index before the move.
     * @return castling right index before the move
     */
	public int getPreviousCastlingRigthIndex() {
		return (data & PREVIOUS_CASTLING_RIGHT_INDEX_MASK) >>> PREVIOUS_CASTLING_RIGHT_INDEX_SHIFT;
	}

    /**
     * Returns EP file before the move.
     * @return EP file before the move
     */
	public int getPreviousEpFile() {
		return (data & PREVIOUS_EP_FILE_MASK) >>> PREVIOUS_EP_FILE_SHIFT;
	}

    /**
     * Writes move in coordinate notation into writer.
     * @param writer PrintWriter
     */
    public void writeCoordinateNotation (final PrintWriter writer) {
    	final int moveType = getMoveType();
    	final int beginSquare = getBeginSquare();
    	final int targetSquare = getTargetSquare();
    	
    	Square.write (writer, beginSquare);
    	Square.write (writer, targetSquare);

    	if (moveType == MoveType.PROMOTION) {
    		final int promotionPieceType = getPromotionPieceType();
    		
    		PieceType.write (writer, promotionPieceType, false);
    	}
    }
    
    /**
     * Converts move into string. Move will be written in coordinate notation.
     * @return notation of the move
     */
    public String toString() {
    	if (getMoveType() != MoveType.INVALID) { 
	    	final StringWriter stringWriter = new StringWriter();
	    	final PrintWriter printWriter = new PrintWriter (stringWriter);
	    	
	    	writeCoordinateNotation(printWriter);
	    	printWriter.flush();
	    	
	    	return stringWriter.toString();
    	}
    	else
    		return "invalid move";
    }
    
    /**
     * Assigns content of given move to this move.
     * @param orig original move
     */
    public void assign (final Move orig) {
    	this.data = orig.data;
    }
    
    /**
     * Returns deep copy of this move.
     * @return deep copy of this move
     */
	public Move copy() {
		final Move move = new Move();
		move.assign(this);
		
		return move;
	}

    /**
     * Returns data of this move.
     * @return move data
     */
	int getData() {
		return data;
	}

	/**
	 * Sets data of the move.
	 * @param data move data
	 */
	void setData (final int data) {
		this.data = data;
	}
	
	/**
	 * Returns minimal data that can uniquely identify move in the position.
	 * @return compressed move
	 */
	public int getCompressedMove() {
		return data & COMPRESSED_MOVE_MASK;
	}
	
	
	
	/**
	 * Tries to uncompress the move.
	 * Move will be checked if it is correct pseudolegal move.
	 * If decompression succeeds method sets the move and returns true, otherwise method clears
	 * the move and returns false.
	 * @param compressedMove compressed move
	 * @param position starting position
	 * @return true if move was uncompressed successfully, false if not
	 */
	public boolean uncompressMove(final int compressedMove, final Position position) {
		final int beginSquare = (compressedMove & BEGIN_SQUARE_MASK) >>> BEGIN_SQUARE_SHIFT;
		final int targetSquare = (compressedMove & TARGET_SQUARE_MASK) >>> TARGET_SQUARE_SHIFT;
		final int promotionPieceType = (compressedMove & PROMOTION_PIECE_TYPE_MASK) >>> PROMOTION_PIECE_TYPE_SHIFT;

		return uncompressMove(beginSquare, targetSquare, promotionPieceType, position);
	}
	
	public boolean uncompressMove(final int beginSquare, final int targetSquare, final int promotionPieceType, final Position position) {
		this.clear();
		
		final int onTurn = position.getOnTurn();
		final long occupancyOnTurn = position.getColorOccupancy(onTurn);
		
		// Pre-checks for correctness
		if (!BitBoard.containsSquare(occupancyOnTurn, beginSquare))
			return false;

		if (BitBoard.containsSquare(occupancyOnTurn, targetSquare))
			return false;
		
		final int movingPieceType = position.getPieceTypeOnSquare(beginSquare);
    	final boolean shouldBePromotion = isPromotion(movingPieceType, targetSquare);
    	
    	if (shouldBePromotion) {
    		if (!PieceType.isPromotionFigure(promotionPieceType))
    			return false;
    	}
    	else {
    		if (promotionPieceType != PieceType.NONE)
    			return false;
    	}
    	
    	// Castling
		final int epFile = position.getEpFile();
		
		if (movingPieceType == PieceType.KING) {
			final CastlingRights castlingRights = position.getCastlingRights();
			
			if (castlingRights.isRightForColor(onTurn) && !position.isCheck()) {
				// Now it is verified that the beginSquare == E1 or E8 (movingPieceType is king and king has not dropped castling rights)
				for (int castlingType = CastlingType.FIRST; castlingType < CastlingType.LAST; castlingType++) {
					if (targetSquare == CastlingConstants.of(onTurn, castlingType).getKingTargetSquare() &&
					    PseudoLegalMoveGenerator.isCastlingPossible(position, castlingType)) {
						
						initialize(position.getCastlingRights().getIndex(), epFile);
			        	setMovingPieceType(movingPieceType);
			        	setBeginSquare (beginSquare);
			    		finishCastling(targetSquare);
			    		
			    		return true;
					}
				}
			}
		}
		
		// EP
		final int oppositeColor = Color.getOppositeColor(onTurn);

		if (movingPieceType == PieceType.PAWN && Square.getFile(targetSquare) == epFile &&
		    Square.getRank(beginSquare) == BoardConstants.getEpRank(oppositeColor) &&
		    BitBoard.containsSquare(PawnAttackTable.getItem(onTurn, beginSquare), targetSquare)) {
			
    		initialize(position.getCastlingRights().getIndex(), epFile);
        	setMovingPieceType(movingPieceType);
        	setBeginSquare (beginSquare);
    		finishEnPassant(targetSquare);
    		
    		return true;			
		}
		
		// Normal move or promotion
		final int capturedPieceType = position.getPieceTypeOnSquare(targetSquare);
		final long allowedSquaresMask;
		
		if (movingPieceType == PieceType.PAWN) {
			if (capturedPieceType == PieceType.NONE)
				allowedSquaresMask = PawnMoveTable.getItem(onTurn, beginSquare);
			else
				allowedSquaresMask = PawnAttackTable.getItem(onTurn, beginSquare);
		}
		else
			allowedSquaresMask = FigureAttackTable.getItem(movingPieceType, beginSquare);
		
    	// Check free space for sliding pieces and pawns
    	if (!PieceType.isShortMovingFigure(movingPieceType)) {
    		final long occupancy = position.getOccupancy();
    		
    		if ((BetweenTable.getItem(beginSquare, targetSquare) & occupancy) != 0)
    			return false;
    	}

		if (!BitBoard.containsSquare(allowedSquaresMask, targetSquare))
			return false;
		
		// Make the normal move or promotion
    	if (shouldBePromotion) {
    		initialize(position.getCastlingRights().getIndex(), epFile);
        	setMovingPieceType(PieceType.PAWN);
        	setBeginSquare (beginSquare);
    		finishPromotion(targetSquare, capturedPieceType, promotionPieceType);
    	}
    	else {
    		initialize(position.getCastlingRights().getIndex(), epFile);
        	setMovingPieceType(movingPieceType);
        	setBeginSquare (beginSquare);
    		finishNormalMove(targetSquare, capturedPieceType);
    	}
		
		return true;
	}

	public static boolean isPromotion(final int movingPieceType, final int targetSquare) {
		return movingPieceType == PieceType.PAWN && BitBoard.containsSquare(BoardConstants.RANK_18_MASK, targetSquare);
	}
	
	@Override
	public boolean equals (final Object obj) {
		if (!(obj instanceof Move))
			return false;
		
		return data == ((Move) obj).data;
	}
	
	@Override
	public int hashCode() {
		return data;
	}

	public boolean isReversible() {
		final int type = getMoveType();
		
		if (type == MoveType.NORMAL) {
			final int movingPieceType = getMovingPieceType();
			final int capturedPieceType = getCapturedPieceType();
			
			return movingPieceType != PieceType.PAWN && capturedPieceType == PieceType.NONE;
		}
		else {
			// Castling, promotion and EP are irreversible
			return false;
		}
	}

	/**
	 * Compares moves in same position by:
	 * - begin square
	 * - target square
	 * - promotion piece type
	 * Do not change the order because book depends on it.
	 */
	@Override
	public int compareTo(final Move that) {
		// Begin square
		final int thisBeginSquare = this.getBeginSquare();
		final int thatBeginSquare = that.getBeginSquare();
		
		if (thisBeginSquare != thatBeginSquare)
			return thisBeginSquare - thatBeginSquare;
		
		// Target square
		final int thisTargetSquare = this.getTargetSquare();
		final int thatTargetSquare = that.getTargetSquare();
		
		if (thisTargetSquare != thatTargetSquare)
			return thisTargetSquare - thatTargetSquare;

		// Promotion piece
		final int thisPromotionPieceType = this.getPromotionPieceType();
		final int thatPromotionPieceType = that.getPromotionPieceType();
		
		if (thisPromotionPieceType != thatPromotionPieceType)
			return thisPromotionPieceType - thatPromotionPieceType;

		return 0;
	}

}
