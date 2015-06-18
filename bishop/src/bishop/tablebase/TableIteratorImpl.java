package bishop.tablebase;

public class TableIteratorImpl extends TableIteratorBase implements ITableIterator {

	private final ITable table;
	
	public TableIteratorImpl(final ITable table, final long beginIndex) {
		super(table.getDefinition(), beginIndex);
		
		this.table = table;
	}

	public TableIteratorImpl(final TableIteratorImpl orig) {
		super (orig);
		
		this.table = orig.table;
	}
	
	@Override
	public int getResult() {
		return table.getResult(getTableIndex());
	}
	
	@Override
	public void setResult(final int result) {
		table.setResult(getTableIndex(), result);
	}

	@Override
	public TableIteratorImpl copy() {
		return new TableIteratorImpl(this);
	}
	
}
