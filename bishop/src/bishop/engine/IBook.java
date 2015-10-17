package bishop.engine;

import java.util.Collection;

import bishop.base.Position;

public interface IBook<T extends BookRecord> {
	/**
	 * Finds record of given position.
	 * @param position position
	 * @return corresponding record or null if not found
	 */
	public T getRecord (final Position position);
	
	/**
	 * Returns collection of all records.
	 * @return collection of all records
	 */
	public Collection<T> getAllRecords();
}
