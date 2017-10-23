package bishop.engine;

import bishop.base.Position;


public final class NullHashTable implements IHashTable {
	/**
	 * Returns hash record for given position.
	 * @param position position
	 * @param record storage for the record
	 * @return true if record is found, false if not
	 */
	@Override
	public boolean getRecord (final Position position, final int expectedHorizon, final HashRecord record) {
		record.setType(HashRecordType.INVALID);
		
		return false;
	}
	
	/**
	 * Updates hash record for given position.
	 * @param position position
	 * @param record hash record
	 */
	@Override
	public void updateRecord (final Position position, final HashRecord record) {
	}
	
	/**
	 * Clears the table.
	 */
	@Override
	public void clear() {
	}
}
