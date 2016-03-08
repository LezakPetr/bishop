package bishop.tablebase;

public interface ITablePage {
	public int getResult (final long index);
	public void setResult (final long index, final int result);
	public long getOffset();
	public long getSize();
	public void read(final ITableIteratorRead it);
	public void clear();
}
