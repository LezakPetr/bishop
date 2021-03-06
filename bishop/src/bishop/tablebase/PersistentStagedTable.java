package bishop.tablebase;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import parallel.Parallel;

/**
 * Staged table that stores data on file system to save memory.
 * @author Ing. Petr Ležák
 */
public class PersistentStagedTable extends StagedTableImpl {

	private static final String INPUT_SUFFIX = ".old";
	private static final String OUTPUT_SUFFIX = ".new";
	
	private final String pathPrefix;
	
	public PersistentStagedTable(final TableDefinition definition, final String pathPrefix) {
		super (definition, false);

		this.pathPrefix = pathPrefix;
	}

	@Override
	public synchronized void switchToModeRead(final Parallel parallel) throws InterruptedException, ExecutionException {
		if (mode == Mode.READ)
			return;
		
		moveOutputToInput();
		
		pages.clear();
		pages.addAll(java.util.Collections.nCopies(pageCount, null));
		
		parallel.parallelFor(0, pageCount, blockIndex -> {
			final ITablePage page = createPage(blockIndex);

			try (
				final InputFileTableIterator it = new InputFileTableIterator(getPagePath(blockIndex) + INPUT_SUFFIX, definition, page.getOffset(), page.getSize())
			) {
				page.read (it);
			}

			pages.set(blockIndex, page);
		});
		
		mode = Mode.READ;
	}
	
	@Override
	public synchronized void switchToModeWrite() {
		if (mode == Mode.WRITE)
			moveOutputToInput();
		
		super.switchToModeWrite();
	}
	
	@Override
	protected IClosableTableIterator getOutputPageIterator(final int pageIndex) throws IOException {
		final String inputPath = getPagePath(pageIndex) + INPUT_SUFFIX;
		final InputFileTableIterator inputIterator;
		
		final long offset = ((long) nextWritePageIndex) << PAGE_SHIFT;
		final long size = Math.min(PAGE_SIZE, definition.getTableIndexCount() - offset);

		if (new java.io.File(inputPath).exists())			
			inputIterator = new InputFileTableIterator(inputPath, definition, offset, size);
		else
			inputIterator = null;

		return new OutputFileTableIterator(inputIterator, getPagePath(pageIndex) + OUTPUT_SUFFIX, definition, offset, size);
	}
	
	public String getPagePath (final int pageIndex) {
		return pathPrefix + pageIndex;
	}
	
	public synchronized void moveOutputToInput() {
		for (int i = 0; i < pageCount; i++) {
			final String prefix = getPagePath(i);
			final java.io.File inputFile = new java.io.File(prefix + INPUT_SUFFIX);
			final java.io.File outputFile = new java.io.File(prefix + OUTPUT_SUFFIX);
			
			inputFile.delete();
			outputFile.renameTo(inputFile);
		}
	}

	public synchronized void clear() {
		for (int i = 0; i < pageCount; i++) {
			final String prefix = getPagePath(i);
			final java.io.File inputFile = new java.io.File(prefix + INPUT_SUFFIX);
			final java.io.File outputFile = new java.io.File(prefix + OUTPUT_SUFFIX);
			
			inputFile.delete();
			outputFile.delete();
		}
	}

}
