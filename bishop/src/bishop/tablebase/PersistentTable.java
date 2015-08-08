package bishop.tablebase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import parallel.IForBody;
import parallel.Parallel;
import bishop.base.Position;

public class PersistentTable implements ITable {

	private static final int BLOCK_SHIFT = 20;
	private static final int BLOCK_SIZE = 1 << BLOCK_SHIFT;
	
	private static final String INPUT_SUFFIX = ".old";
	private static final String OUTPUT_SUFFIX = ".new";
	
	private enum Mode {
		READ,
		WRITE
	};
	
	private static class TableReadBlock {
		private final long offset;
		private final short[] results;
		
		public TableReadBlock (final long offset, final int size) {
			this.offset = offset;
			this.results = new short[size];
		}
		
		public int getResult (final long index) {
			return results[(int) (index - offset)];
		}
		
		public long getOffset() {
			return offset;
		}

		public void read(final ITableIteratorRead it) {
			for (int i = 0; i < results.length; i++) {
				results[i] = (short) it.getResult();
				it.next();
			}
		}
	}

	private final List<TableReadBlock> readBlock;
	private final TableDefinition definition;
	private final String pathPrefix;
	private final int blockCount; 
	private Mode mode;
	private int nextWriteBlockIndex;
	
	public PersistentTable(final TableDefinition definition, final String pathPrefix) {
		this.definition = definition;
		this.pathPrefix = pathPrefix;
		this.blockCount = getBlockCount();
		
		this.readBlock = new ArrayList<>(blockCount);
	}

	@Override
	public TableDefinition getDefinition() {
		return definition;
	}
	
	private TableReadBlock getBlock (final long index) {
		final int blockIndex = (int) (index >>> BLOCK_SHIFT);
		
		return readBlock.get(blockIndex);
	}

	@Override
	public int getResult(final long index) {
		if (mode != Mode.READ)
			throw new RuntimeException("Table is in mode " + mode);
		
		if (index < 0)
			return TableResult.ILLEGAL;
		
		final TableReadBlock block = getBlock(index);
		
		return block.getResult(index);
	}

	@Override
	public int getPositionResult(final Position position) {
		final long index = definition.calculateTableIndex(position);
		
		return getResult (index);
	}

	@Override
	public ITableIterator getIterator() {
		if (mode != Mode.READ)
			throw new RuntimeException();
		
		return new TableIteratorImpl(this, 0);
	}
	
	public synchronized void switchToModeRead(final Parallel parallel) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		if (mode == Mode.READ)
			return;
		
		readBlock.clear();
		readBlock.addAll(java.util.Collections.<TableReadBlock>nCopies(blockCount, null));
		
		parallel.parallelFor(0, blockCount, new IForBody() {
			@Override
			public void run(final int blockIndex) throws Exception {
				final long offset = ((long) blockIndex) << BLOCK_SHIFT;
				final int size = (int) Math.min(definition.getTableIndexCount() - offset, BLOCK_SIZE);
				
				final TableReadBlock block = new TableReadBlock(offset, size);
				
				try (
					final InputFileTableIterator it = new InputFileTableIterator(getBlockPath(blockIndex) + INPUT_SUFFIX, definition, block.getOffset(), size)
				) {
					block.read (it);
				}
				
				readBlock.set(blockIndex, block);
			}
		});
		
		mode = Mode.READ;
	}

	private int getBlockCount() {
		final long tableIndexCount = definition.getTableIndexCount();
		final int blockCount = (int) ((tableIndexCount + BLOCK_SIZE - 1) >> BLOCK_SHIFT);
		
		return blockCount;
	}
	
	public synchronized void switchToModeWrite() {
		readBlock.clear();
		
		nextWriteBlockIndex = 0;
		mode = Mode.WRITE;
	}
	
	public synchronized OutputFileTableIterator getOutputBlock() throws FileNotFoundException {
		if (nextWriteBlockIndex >= blockCount)
			return null;
		
		final long offset = ((long) nextWriteBlockIndex) << BLOCK_SHIFT;
		final long size = Math.min(BLOCK_SIZE, definition.getTableIndexCount() - offset);
		final String inputPath = getBlockPath(nextWriteBlockIndex) + INPUT_SUFFIX;
		final InputFileTableIterator inputIterator;
		
		if (new java.io.File(inputPath).exists())
			inputIterator = new InputFileTableIterator(inputPath, definition, offset, size);
		else
			inputIterator = null;
		
		final OutputFileTableIterator outputIterator = new OutputFileTableIterator(inputIterator, getBlockPath(nextWriteBlockIndex) + OUTPUT_SUFFIX, definition, offset, size);
		nextWriteBlockIndex++;
		
		return outputIterator;
	}
	
	public String getBlockPath (final int blockIndex) {
		return pathPrefix + blockIndex;
	}
	
	public synchronized void moveOutputToInput() {
		for (int i = 0; i < blockCount; i++) {
			final String prefix = getBlockPath(i);
			final java.io.File inputFile = new java.io.File(prefix + INPUT_SUFFIX);
			final java.io.File outputFile = new java.io.File(prefix + OUTPUT_SUFFIX);
			
			inputFile.delete();
			outputFile.renameTo(inputFile);
		}
	}

	public synchronized void clear() {
		for (int i = 0; i < blockCount; i++) {
			final String prefix = getBlockPath(i);
			final java.io.File inputFile = new java.io.File(prefix + INPUT_SUFFIX);
			final java.io.File outputFile = new java.io.File(prefix + OUTPUT_SUFFIX);
			
			inputFile.delete();
			outputFile.delete();
		}
	}

	@Override
	public void setResult(final long tableIndex, final int result) {
		throw new RuntimeException("PersistentTable.setResult not implemented");
	}

}
