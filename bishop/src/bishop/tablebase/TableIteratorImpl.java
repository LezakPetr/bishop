package bishop.tablebase;

public class TableIteratorImpl extends TableIteratorBase<ITable> implements ITableIterator {

	public TableIteratorImpl(final ITable table, final long beginIndex) {
		super(table, beginIndex);
	}

	public TableIteratorImpl(final TableIteratorImpl orig) {
		super (orig);
	}
	
	@Override
	public void setResult(final int result) {
		final ITable table = getTable();
		
		table.setResult(getTableIndex(), result);
	}

	@Override
	public TableIteratorImpl copy() {
		return new TableIteratorImpl(this);
	}
	
}
