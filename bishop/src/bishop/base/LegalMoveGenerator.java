package bishop.base;

public class LegalMoveGenerator implements IMoveGenerator {
	private final PseudoLegalMoveGenerator backgroundGenerator;
	private Position beginPosition;
	private IMoveWalker userWalker;
	
	public MoveGeneratorType getGeneratorType() {
		return MoveGeneratorType.DIRECT;
	}

	/**
	 * Default constructor.
	 */
	public LegalMoveGenerator() {
		backgroundGenerator = new PseudoLegalMoveGenerator();
		backgroundGenerator.setWalker(this::processMove);
	}

	private boolean processMove(final Move move) {
		final boolean isLegal = beginPosition.isLegalMove(move);

		if (!isLegal)
			return true;

		return userWalker.processMove(move);
	}
	
	/**
     * Generates all legal moves in the position.
     * Calls walker for each generated move.
     */
    public void generateMoves() {
    	backgroundGenerator.generateMoves();
    }
    
    /**
     * Sets position for move generation.
     * Given object may be used - it may or may not be copied.
     * @param position position where moves will be generated
     */
    public void setPosition(final Position position) {
    	this.beginPosition = position;
    	
    	backgroundGenerator.setPosition(this.beginPosition);
    }

    /**
     * Sets walker that will be called when move is generated.
     * @param walker walker of moves
     */
    public void setWalker (final IMoveWalker walker) {
    	this.userWalker = walker;
    }
    
    /**
     * Setting to true means that the generator will not generate moves that cannot stop check. 
     * @param reduce true in case that moves will be reduced in case of check
     */
    public void setReduceMovesInCheck(final boolean reduce) {
    	backgroundGenerator.setReduceMovesInCheck(reduce);
    }
    
	/**
	 * Instructs the generator to (not) generate moves of given piece.
	 * @param pieceType piece type
	 * @param generate generate or not
	 */
	public void setGenerateMovesOfPiece (final int pieceType, final boolean generate) {
		backgroundGenerator.setGenerateMovesOfPiece(pieceType, generate);
	}

}
