package bishop.tablebase;


import parallel.Parallel;

public class MemoryStagedTable extends StagedTableImpl {
	
	public MemoryStagedTable(final TableDefinition definition, final boolean compressedPages) {
		super(definition, compressedPages);
		
		this.pages.clear();
		
		for (int i = 0; i < pageCount; i++) {
			this.pages.add(createPage(i));
		}
	}

	@Override
	public synchronized void clear() {
		for (int i = 0; i < pageCount; i++)
			this.pages.get(i).clear();
	}

	@Override
	public synchronized void switchToModeRead(final Parallel parallel) {
		mode = Mode.READ;
	}

	@Override
	public synchronized void switchToModeWrite() {
		nextWritePageIndex = 0;
		mode = Mode.WRITE;
	}

	@Override
	protected IClosableTableIterator getOutputPageIterator(final int pageIndex) {
		return new TablePageIterator(definition, pages.get(pageIndex));
	}

}
