package bishop.tablebase;

/**
 * Interface that represents read-only table.
 * Table represents mapping position -> index -> result.
 * @author Ing. Petr Ležák
 */
public interface ITableRead extends IPositionResultSource {
	/**
	 * Returns read-only iterator that can be used to iterate thru table.
	 * @return table iterator
	 */
	public ITableIteratorRead getIterator();
	
	/**
	 * Returns table definition.
	 * @return table definition
	 */
	public TableDefinition getDefinition();
	
	/**
	 * Returns result on given index.
	 * @param index index of the result
	 * @return result
	 */
	public int getResult(final long index);	 
}
