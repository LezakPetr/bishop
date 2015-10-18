package bishop.tablebase;

import java.io.IOException;

public class TablePageIterator extends TableIteratorBase implements IClosableTableIterator {
	
	private final TablePage page;
	
	public TablePageIterator(final TableDefinition tableDefinition, final TablePage page) {
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
	public void close() throws IOException {
		// No operation
	}

}