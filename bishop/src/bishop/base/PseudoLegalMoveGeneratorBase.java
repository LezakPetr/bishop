package bishop.base;

public abstract class PseudoLegalMoveGeneratorBase implements IMoveGenerator {
	
	protected Position position;
	protected IMoveWalker walker;
	protected final Move move = new Move();
	

	public MoveGeneratorType getGeneratorType() {
		return MoveGeneratorType.DIRECT;
	}
	
    // Generates en-passant moves.
    // Returns if generation should continue.
    protected final boolean generateEnPassantMoves() {
    	final int onTurn = position.getOnTurn();
    	final int epFile = position.getEpFile();

    	if (epFile == File.NONE)
    		return true;

    	final long pawnMask = position.getPiecesMask (onTurn, PieceType.PAWN);
    	
    	move.setMovingPieceType(PieceType.PAWN);

    	for (int direction = EpDirection.FIRST; direction < EpDirection.LAST; direction++) {
            final EpMoveRecord record = PieceMoveTables.getEpMoveRecord(onTurn, epFile, direction);
            
            if (record != null) {
            	final int beginSquare = record.getBeginSquare();
            
            	if ((pawnMask & BitBoard.getSquareMask (beginSquare)) != 0) {
            		move.setBeginSquare(beginSquare);
            		move.finishEnPassant (record.getTargetSquare());

            		if (!walker.processMove(move))
            			return false;
            	}
            }
    	}

    	return true;
    }

    /**
     * Sets position for move generation.
     * Given object may be used - it may or may not be copied.
     * @param position position where moves will be generated
     */
    public void setPosition(final Position position) {
    	this.position = position;
    }

    /**
     * Sets walker that will be called when move is generated.
     * @param walker walker of moves
     */
    public void setWalker (final IMoveWalker walker) {
    	this.walker = walker;
    }

}
