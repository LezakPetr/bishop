package bishop.tablebase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bishop.tablebase.StagedTableImpl.Mode;


import parallel.Parallel;

public class MemoryStagedTable extends StagedTableImpl {
	
	public MemoryStagedTable(final TableDefinition definition) {
		super(definition);
		
		this.readPages.clear();
		
		for (int i = 0; i < pageCount; i++) {
			final long offset = ((long) i) << PAGE_SHIFT;
			final int size = (int) Math.min(definition.getTableIndexCount() - offset, PAGE_SIZE);
			
			this.readPages.add(new TablePage(offset, size));
		}
	}

	@Override
	public synchronized void clear() {
		// No operation
	}

	@Override
	public synchronized void moveOutputToInput() {
	}

	@Override
	public synchronized void switchToModeRead(final Parallel parallel) throws IOException, InterruptedException, ExecutionException {
		mode = Mode.READ;
	}

	@Override
	public synchronized void switchToModeWrite() {
		if (mode == Mode.WRITE)
			return;
		
		nextWritePageIndex = 0;
		mode = Mode.WRITE;
	}

	@Override
	protected IClosableTableIterator getOutputBlockIterator(final int pageIndex, final long offset, final long size) throws IOException {
		return new TablePageIterator(definition, readPages.get(pageIndex));
	}

}
