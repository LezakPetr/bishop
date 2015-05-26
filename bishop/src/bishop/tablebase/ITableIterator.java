package bishop.tablebase;


public interface ITableIterator extends ITableIteratorRead {
	public void setResult(final int result);
	public ITableIterator copy();
}
