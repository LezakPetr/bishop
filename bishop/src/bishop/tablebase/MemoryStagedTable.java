package bishop.tablebase;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


import parallel.Parallel;

public class MemoryStagedTable extends StagedTableImpl {
	
	public MemoryStagedTable(final TableDefinition definition) {
		super(definition);
		
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
	public synchronized void switchToModeRead(final Parallel parallel) throws IOException, InterruptedException, ExecutionException {
		mode = Mode.READ;
	}

	@Override
	public synchronized void switchToModeWrite() {
		nextWritePageIndex = 0;
		mode = Mode.WRITE;
	}

	@Override
	protected IClosableTableIterator getOutputPageIterator(final int pageIndex) throws IOException {
		return new TablePageIterator(definition, pages.get(pageIndex));
	}

}
