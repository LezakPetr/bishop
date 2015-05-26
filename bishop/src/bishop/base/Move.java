package bishop.base;

import java.io.PrintWriter;
import java.io.StringWriter;

import bishop.tables.BetweenTable;
import bishop.tables.FigureAttackTable;
import bishop.tables.PawnAttackTable;
import bishop.tables.PawnMoveTable;

public final class Move {

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
	
	
	private static final int COMPRESSED_MOVE_MASK = BEGIN_SQUARE_MASK | TARGET_SQUARE_MASK | PROMOTION_PIECE_TYPE_MASK;
	
	public static final int NONE_COMPRESSED_MOVE = 0;
	
	
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
	 * Move will be checked if it is correct pseudolegal move. Not all correct moves can be decompressed.
	 * If decompression succeeds method sets the move and returns true, otherwise method clears
	 * the move and returns false.
	 * @param compressedMove compressed move
	 * @param position starting position
	 * @return true if move was uncompressed successfully, false if not
	 */
	public boolean uncompressMove(final int compressedMove, final Position position) {
		this.clear();
		
		final int beginSquare = (compressedMove & BEGIN_SQUARE_MASK) >>> BEGIN_SQUARE_SHIFT;
		final int targetSquare = (compressedMove & TARGET_SQUARE_MASK) >>> TARGET_SQUARE_SHIFT;
		final int promotionPieceType = (compressedMove & PROMOTION_PIECE_TYPE_MASK) >>> PROMOTION_PIECE_TYPE_SHIFT;
		
		final int onTurn = position.getOnTurn();
		final int oppositeColor = Color.getOppositeColor(onTurn);
		final Piece movingPiece = position.getSquareContent(beginSquare);
		
		if (movingPiece == null || movingPiece.getColor() != onTurn)
			return false;
		
		final Piece capturedPiece = position.getSquareContent(targetSquare);
		
		if (capturedPiece != null && capturedPiece.getColor() != oppositeColor)
			return false;
		
		final int movingPieceType = movingPiece.getPieceType();
		long allowedSquaresMask;
		
		if (movingPiece.getPieceType() == PieceType.PAWN) {
			if (capturedPiece == null)
				allowedSquaresMask = PawnMoveTable.getItem(onTurn, beginSquare);
			else
				allowedSquaresMask = PawnAttackTable.getItem(onTurn, beginSquare);
		}
		else {
			allowedSquaresMask = FigureAttackTable.getItem(movingPieceType, beginSquare);
		}
		
		final long targetSquareMask = BitBoard.getSquareMask(targetSquare);
		
		if ((allowedSquaresMask & targetSquareMask) == 0)
			return false;
		
    	// Check free space for sliding pieces
    	if (!PieceType.isShortMovingFigure(movingPieceType)) {
    		final long occupancy = position.getOccupancy();
    		
    		if ((BetweenTable.getItem(beginSquare, targetSquare) & occupancy) != 0)
    			return false;
    	}
    	
    	final boolean shouldBePromotion = isPromotion(movingPieceType, targetSquare);
    	final int capturedPieceType = (capturedPiece == null) ? PieceType.NONE : capturedPiece.getPieceType();
    	
    	if (promotionPieceType == PieceType.NONE) {
    		if (shouldBePromotion)
    			return false;
    		
    		initialize(position.getCastlingRights().getIndex(), position.getEpFile());
        	setMovingPieceType(movingPieceType);
        	setBeginSquare (beginSquare);
    		finishNormalMove(targetSquare, capturedPieceType);
    	}
    	else {
    		if (!shouldBePromotion)
    			return false;

    		initialize(position.getCastlingRights().getIndex(), position.getEpFile());
        	setMovingPieceType(PieceType.PAWN);
        	setBeginSquare (beginSquare);
    		finishPromotion(targetSquare, capturedPieceType, promotionPieceType);
    	}
		
		return true;
	}

	public static boolean isPromotion(final int movingPieceType, final int targetSquare) {
		final long targetSquareMask = BitBoard.getSquareMask(targetSquare);
		
		return movingPieceType == PieceType.PAWN && (targetSquareMask & BoardConstants.RANK_18_MASK) != 0;
	}
	
	public boolean equals (final Object obj) {
		if (!(obj instanceof Move))
			return false;
		
		return data == ((Move) obj).data;
	}
	
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

}
