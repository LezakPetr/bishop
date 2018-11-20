package bishop.base;

/**
 * BitLoop is auxiliary class that returns series of squares from given bit board with set bit.
 * @author Bc. Petr Ležák
 */
public final class BitLoop {

	// Board with still unread squares
	private long board;

	
	/**
	 * Initializes loop above given bit board.
	 * @param board bit board
	 */
	public BitLoop (final long board) {
		this.board = board;
	}
	
	public BitLoop () {
	}
	
	public void init (final long board) {
		this.board = board;
	}
	
	/**
	 * Checks if there is some another square with set bit.
	 * @return true if there is another square, false if not
	 */
	public boolean hasNextSquare() {
		return board != 0;
	}
	
	/**
	 * Returns next set bit.
	 * If there is no more bit set result is undefined.
	 * @return square with set bit
	 */
	public int getNextSquare() {
		final int square = Long.numberOfTrailingZeros(board);
		board &= board - 1;
		
		return square;
	}
	
	/**
	 * Assigns given original loop to this loop.
	 * This loop will return same squares like the original one.
	 * @param orig original loop
	 */
	public void assign (final BitLoop orig) {
		this.board = orig.board;
	}
	
}
