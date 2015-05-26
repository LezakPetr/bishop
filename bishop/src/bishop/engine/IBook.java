package bishop.engine;

import bishop.base.Position;

public interface IBook {
	/**
	 * Finds record of given position.
	 * @param position position
	 * @return corresponding record or null if not found
	 */
	public BookRecord getRecord (final Position position);
}
