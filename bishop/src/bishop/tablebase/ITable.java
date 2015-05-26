package bishop.tablebase;

/**
 * Interface that represents read/write table.
 * Table represents mapping position -> index -> result.
 * @author Ing. Petr Ležák
 */
public interface ITable extends ITableRead {
	/**
	 * Returns read/write iterator that can be used to iterate thru table.
	 * @return table iterator
	 */
	public ITableIterator getIterator();
	
	/**
	 * Sets result on given index.
	 * @param index index of the result
	 * @param result new result
	 */
	public void setResult(final long index, final int result);
}
