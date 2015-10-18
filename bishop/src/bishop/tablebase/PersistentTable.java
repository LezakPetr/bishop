package bishop.tablebase;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import parallel.IForBody;
import parallel.Parallel;

public class PersistentTable extends StagedTableImpl {

	private static final String INPUT_SUFFIX = ".old";
	private static final String OUTPUT_SUFFIX = ".new";
	
	private final String pathPrefix;
	
	public PersistentTable(final TableDefinition definition, final String pathPrefix) {
		super (definition);

		this.pathPrefix = pathPrefix;
	}

	@Override
	public synchronized void switchToModeRead(final Parallel parallel) throws IOException, InterruptedException, ExecutionException {
		if (mode == Mode.READ)
			return;
		
		readPages.clear();
		readPages.addAll(java.util.Collections.<TablePage>nCopies(pageCount, null));
		
		parallel.parallelFor(0, pageCount, new IForBody() {
			@Override
			public void run(final int blockIndex) throws Exception {
				final long offset = ((long) blockIndex) << PAGE_SHIFT;
				final int size = (int) Math.min(definition.getTableIndexCount() - offset, PAGE_SIZE);
				
				final TablePage block = new TablePage(offset, size);
				
				try (
					final InputFileTableIterator it = new InputFileTableIterator(getBlockPath(blockIndex) + INPUT_SUFFIX, definition, block.getOffset(), size)
				) {
					block.read (it);
				}
				
				readPages.set(blockIndex, block);
			}
		});
		
		mode = Mode.READ;
	}
		
	protected IClosableTableIterator getOutputBlockIterator(final int blockIndex, final long offset, final long size) throws IOException {
		final String inputPath = getBlockPath(blockIndex) + INPUT_SUFFIX;
		final InputFileTableIterator inputIterator;
		
		if (new java.io.File(inputPath).exists())
			inputIterator = new InputFileTableIterator(inputPath, definition, offset, size);
		else
			inputIterator = null;
		
		final OutputFileTableIterator outputIterator = new OutputFileTableIterator(inputIterator, getBlockPath(blockIndex) + OUTPUT_SUFFIX, definition, offset, size);

		return outputIterator;
	}
	
	public String getBlockPath (final int blockIndex) {
		return pathPrefix + blockIndex;
	}
	
	public synchronized void moveOutputToInput() {
		for (int i = 0; i < pageCount; i++) {
			final String prefix = getBlockPath(i);
			final java.io.File inputFile = new java.io.File(prefix + INPUT_SUFFIX);
			final java.io.File outputFile = new java.io.File(prefix + OUTPUT_SUFFIX);
			
			inputFile.delete();
			outputFile.renameTo(inputFile);
		}
	}

	public synchronized void clear() {
		for (int i = 0; i < pageCount; i++) {
			final String prefix = getBlockPath(i);
			final java.io.File inputFile = new java.io.File(prefix + INPUT_SUFFIX);
			final java.io.File outputFile = new java.io.File(prefix + OUTPUT_SUFFIX);
			
			inputFile.delete();
			outputFile.delete();
		}
	}

}
