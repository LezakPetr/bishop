package bishop.base;

public interface IMoveGenerator {
		
    /**
     * Generates all legal moves in the position.
     * Calls walker for each generated move.
     */
    public void generateMoves();
    
    /**
     * Sets position for move generation.
     * Given object may be used - it may or may not be copied.
     * @param position position where moves will be generated
     */
    public void setPosition(final Position position);

    /**
     * Sets walker that will be called when move is generated.
     * @param walker walker of moves
     */
    public void setWalker (final IMoveWalker walker);
    
	/**
	 * Returns generator type.
	 * @return generator type
	 */
	public MoveGeneratorType getGeneratorType();
}
