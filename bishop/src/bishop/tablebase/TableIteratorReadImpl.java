package bishop.tablebase;

public class TableIteratorReadImpl extends TableIteratorBase<ITableRead> {

	public TableIteratorReadImpl(final ITableRead table, final long beginIndex) {
		super(table, beginIndex);
	}

	public TableIteratorReadImpl(final TableIteratorReadImpl orig) {
		super (orig);
	}

	@Override
	public TableIteratorReadImpl copy() {
		return new TableIteratorReadImpl(this);
	}
	
}
