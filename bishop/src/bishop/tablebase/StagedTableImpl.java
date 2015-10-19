package bishop.tablebase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bishop.base.Position;

import parallel.Parallel;

/**
 * Base class for staged tables.
 * @author Ing. Petr Ležák
 */
public abstract class StagedTableImpl implements IStagedTable {
	protected static final int PAGE_SHIFT = 20;
	protected static final int PAGE_SIZE = 1 << PAGE_SHIFT;
	
	protected enum Mode {
		READ,
		WRITE
	};

	protected final List<TablePage> pages;
	protected final TableDefinition definition;
	protected final int pageCount; 
	protected Mode mode;
	protected int nextWritePageIndex;
	
	public StagedTableImpl(final TableDefinition definition) {
		this.definition = definition;
		this.pageCount = getPageCount();
		
		this.pages = new ArrayList<>(pageCount);
	}

	@Override
	public TableDefinition getDefinition() {
		return definition;
	}
	
	private TablePage getPage (final long index) {
		final int pageIndex = (int) (index >>> PAGE_SHIFT);
		
		return pages.get(pageIndex);
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
	public synchronized IClosableTableIterator getOutputPage() throws IOException {
		if (nextWritePageIndex >= pageCount)
			return null;
		
		final IClosableTableIterator iterator = getOutputPageIterator (nextWritePageIndex);
		nextWritePageIndex++;
		
		return iterator;
	}
	
	@Override
	public synchronized void switchToModeWrite() {
		pages.clear();
		
		nextWritePageIndex = 0;
		mode = Mode.WRITE;
	}
	
	protected TablePage createPage(final int pageIndex) {
		final long offset = ((long) pageIndex) << PAGE_SHIFT;
		final int size = (int) Math.min(definition.getTableIndexCount() - offset, PAGE_SIZE);
		final TablePage page = new TablePage(offset, size);
		
		return page;
	}
	
	@Override
	public abstract void switchToModeRead(final Parallel parallel) throws IOException, InterruptedException, ExecutionException;

	protected abstract IClosableTableIterator getOutputPageIterator(final int pageIndex) throws IOException;

}
