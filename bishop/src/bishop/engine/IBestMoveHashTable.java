package bishop.engine;

import bishop.base.Position;

public interface IBestMoveHashTable {
	/**
	 * Returns hash record for given position.
	 * @param position position
	 * @return true if record is found, false if not
	 */
	public int getRecord (final Position position);

	/**
	 * Updates hash record for given position.
	 * @param position position
	 * @param horizon horizon
	 * @param compressedBestMove compressed best move
	 */
	public void updateRecord (final Position position, final int horizon, final int compressedBestMove);

	/**
	 * Clears the table.
	 */
	public void clear();
}
