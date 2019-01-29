package bishop.tablebase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bishop.base.IPosition;

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

	protected final List<ITablePage> pages;
	protected final TableDefinition definition;
	protected final int pageCount; 
	protected Mode mode;
	protected int nextWritePageIndex;
	private final boolean compressedPages;
	
	public StagedTableImpl(final TableDefinition definition, final boolean compressedPages) {
		this.definition = definition;
		this.pageCount = getPageCount();
		
		this.pages = new ArrayList<>(pageCount);
		this.compressedPages = compressedPages;
	}

	@Override
	public TableDefinition getDefinition() {
		return definition;
	}
	
	private ITablePage getPage (final long index) {
		final int pageIndex = (int) (index >>> PAGE_SHIFT);
		
		return pages.get(pageIndex);
	}

	@Override
	public int getResult(final long index) {
		if (mode != Mode.READ)
			throw new RuntimeException("Table is in mode " + mode);
		
		if (index < 0)
			return TableResult.ILLEGAL;
		
		final ITablePage page = getPage(index);
		
		return page.getResult(index);
	}
	
	private int getPageCount() {
		final long tableIndexCount = definition.getTableIndexCount();

		return (int) ((tableIndexCount + PAGE_SIZE - 1) >> PAGE_SHIFT);
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
	public int getPositionResult(final IPosition position) {
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
	
	protected ITablePage createPage(final int pageIndex) {
		final long offset = ((long) pageIndex) << PAGE_SHIFT;
		final int size = (int) Math.min(definition.getTableIndexCount() - offset, PAGE_SIZE);
		
		if (compressedPages)
			return new CompressedTablePage(offset, size);
		else
			return new FullTablePage(offset, size);
	}
	
	@Override
	public abstract void switchToModeRead(final Parallel parallel) throws InterruptedException, ExecutionException;

	protected abstract IClosableTableIterator getOutputPageIterator(final int pageIndex) throws IOException;

}
