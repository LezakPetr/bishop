package bishop.base;

public class LegalMoveGenerator implements IMoveGenerator {
	private PseudoLegalMoveGenerator backgroundGenerator;
	private Position beginPosition;
	private IMoveWalker userWalker;
	
	public MoveGeneratorType getGeneratorType() {
		return MoveGeneratorType.DIRECT;
	}

	private IMoveWalker backgroundWalker = new IMoveWalker() {
		public boolean processMove(final Move move) {
			final boolean isLegal = beginPosition.isLegalMove(move);

			if (!isLegal)
				return true;
			
			return userWalker.processMove(move);
		}
	};

	/**
	 * Default constructor.
	 */
	public LegalMoveGenerator() {
		backgroundGenerator = new PseudoLegalMoveGenerator();
		backgroundGenerator.setWalker(backgroundWalker);
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

}
