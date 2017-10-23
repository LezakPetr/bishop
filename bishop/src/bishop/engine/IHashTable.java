package bishop.engine;

import bishop.base.Position;

public interface IHashTable {
	/**
	 * Returns hash record for given position.
	 * @param position position
	 * @param record storage for the record
	 * @return true if record is found, false if not
	 */
	public boolean getRecord (final Position position, final int expectedHorizon, final HashRecord record);
	
	/**
	 * Updates hash record for given position.
	 * @param position position
	 * @param record hash record
	 */
	public void updateRecord (final Position position, final HashRecord record);
	
	/**
	 * Clears the table.
	 */
	public void clear();
}
