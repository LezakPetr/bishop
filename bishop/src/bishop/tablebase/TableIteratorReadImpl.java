package bishop.tablebase;

public class TableIteratorReadImpl extends TableIteratorBase {

	private final ITableRead table;
	

	public TableIteratorReadImpl(final ITableRead table, final long beginIndex) {
		super(table.getDefinition(), beginIndex);
		
		this.table = table;
	}

	public TableIteratorReadImpl(final TableIteratorReadImpl orig) {
		super (orig);
		
		this.table = orig.table;
	}

	@Override
	public TableIteratorReadImpl copy() {
		return new TableIteratorReadImpl(this);
	}
	
	@Override
	public int getResult() {
		return table.getResult(getTableIndex());
	}

}
