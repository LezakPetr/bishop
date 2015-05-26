package bishop.tablebase;

import bishop.base.Position;

public interface ITableIteratorRead {
	public boolean isValid();
	public void next();
	public void moveForward(final long count);
	
	public void fillPosition (final Position position);
	public int getResult();
	public long getTableIndex();
	public int getChunkIndex();
	
	/**
	 * Returns total number of positions walked by this iterator.
	 * @return position count
	 */
	public long getPositionCount();
	
	public ITableIteratorRead copy();
}
