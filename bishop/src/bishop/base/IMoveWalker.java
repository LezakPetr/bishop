package bishop.base;


/**
 * MoveWalker is interface that can process moves.
 * @author Bc. Petr Ležák
 */
public interface IMoveWalker {
	/**
	 * Processes given move.
	 * @param move move to process; move is temporary and can be changed after method returns
	 * @return  true if move generation should continue, false if no more moves are needed
	 */
	public boolean processMove(final Move move);
}
