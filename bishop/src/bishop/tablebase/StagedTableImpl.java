package bishop.tablebase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bishop.base.Position;

import parallel.Parallel;

public abstract class StagedTableImpl implements IStagedTable {
	protected static final int PAGE_SHIFT = 20;
	protected static final int PAGE_SIZE = 1 << PAGE_SHIFT;
	
	protected enum Mode {
		READ,
		WRITE
	};

	protected final List<TablePage> readPages;
	protected final TableDefinition definition;
	protected final int pageCount; 
	protected Mode mode;
	protected int nextWritePageIndex;
	
	public StagedTableImpl(final TableDefinition definition) {
		this.definition = definition;
		this.pageCount = getPageCount();
		
		this.readPages = new ArrayList<>(pageCount);
	}

	@Override
	public TableDefinition getDefinition() {
		return definition;
	}
	
	private TablePage getPage (final long index) {
		final int pageIndex = (int) (index >>> PAGE_SHIFT);
		
		return readPages.get(pageIndex);
	}

	@Override
	public int getResult(final long index) {
		if (mode != Mode.READ)
			throw new RuntimeException("Table is in mode " + mode);
		
		if (index < 0)
			return TableResult.ILLEGAL;
		
		final TablePage page = getPage(index);
		
		return page.getResult(index);
	}
	
	private int getPageCount() {
		final long tableIndexCount = definition.getTableIndexCount();
		final int pageCount = (int) ((tableIndexCount + PAGE_SIZE - 1) >> PAGE_SHIFT);
		
		return pageCount;
	}

	@Override
	public void setResult(final long tableIndex, final int result) {
		throw new RuntimeException("StagedTableImpl.setResult not implemented");
	}
	
	@Override
	public ITableIterator getIterator() {
		if (mode != Mode.READ)
			throw new RuntimeException();
		
		return new TableIteratorImpl(this, 0);
	}
	
	@Override
	public int getPositionResult(final Position position) {
		final long index = definition.calculateTableIndex(position);
		
		return getResult (index);
	}
	
	@Override
	public synchronized IClosableTableIterator getOutputBlock() throws IOException {
		if (nextWritePageIndex >= pageCount)
			return null;
		
		final long offset = ((long) nextWritePageIndex) << PAGE_SHIFT;
		final long size = Math.min(PAGE_SIZE, definition.getTableIndexCount() - offset);
		
		final IClosableTableIterator iterator = getOutputBlockIterator (nextWritePageIndex, offset, size);
		nextWritePageIndex++;
		
		return iterator;
	}
	
	@Override
	public synchronized void switchToModeWrite() {
		readPages.clear();
		
		nextWritePageIndex = 0;
		mode = Mode.WRITE;
	}
	
	@Override
	public abstract void switchToModeRead(final Parallel parallel) throws IOException, InterruptedException, ExecutionException;

	protected abstract IClosableTableIterator getOutputBlockIterator(final int blockIndex, final long offset, final long size) throws IOException;

}
