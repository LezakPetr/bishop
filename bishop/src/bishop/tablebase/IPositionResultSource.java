package bishop.tablebase;

import bishop.base.IPosition;

/**
 * Interface that represents mapping position -> result.
 * @author Ing. Petr Ležák
 */
public interface IPositionResultSource {
	/**
	 * Returns result for given position.
	 * @param position position
	 * @return result for position
	 */
	public int getPositionResult(final IPosition position);
}
