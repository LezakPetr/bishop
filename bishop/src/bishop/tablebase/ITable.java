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

	public void setResult(final long tableIndex, final int result);
}
