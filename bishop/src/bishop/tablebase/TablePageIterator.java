package bishop.tablebase;

public class TablePageIterator extends TableIteratorBase implements IClosableTableIterator {
	
	private final ITablePage page;
	
	public TablePageIterator(final TableDefinition tableDefinition, final ITablePage page) {
		super(tableDefinition, page.getOffset());
		
		this.page = page;
	}

	@Override
	public void setResult(final int result) {
		page.setResult(getTableIndex(), result);
	}
	
	@Override
	public int getResult() {
		return page.getResult(getTableIndex());
	}

	@Override
	public OutputFileTableIterator copy() {
		throw new RuntimeException("Method TablePageIterator.copy is not implemented");
	}

	@Override
	public void close() {
		// No operation
	}

	@Override
	public boolean isValid() {
		if (!super.isValid())
			return false;
		
		return page == null || getTableIndex() < page.getOffset() + page.getSize();
	}

}
