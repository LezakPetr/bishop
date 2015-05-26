package bishop.tablebase;

import java.io.File;
import java.io.IOException;

import bishop.base.Position;

public class FilePositionResultSource implements ITableRead {
	
	private final TableReader reader;
	private final TableDefinition definition;
	private final TableBlockCache blockCache;
	
	public FilePositionResultSource (final File file, final TableBlockCache blockCache) {
		this.reader = new TableReader(file);
		this.definition = reader.getDefinition();
		this.blockCache = blockCache;
	}
	
	private ITable getBlockWithResult (final long tableIndex) throws IOException {
		final long blockIndex = reader.getBlockIndex(tableIndex);
		final BlockKey key = new BlockKey(definition.getMaterialHash(), blockIndex);
		ITable block = blockCache.getBlock(key);
		
		if (block != null) {
			return block;
		}
		
		reader.readBlockWithResult(tableIndex);
		block = reader.getTable();
		
		blockCache.addBlock(key, block);
		
		return block;
	}

	@Override
	public synchronized int getPositionResult(final Position position) {
		final long tableIndex = definition.calculateTableIndex(position);
		
		return getResult(tableIndex);
	}

	@Override
	public ITableIteratorRead getIterator() {
		return new TableIteratorReadImpl(this, 0);
	}

	@Override
	public TableDefinition getDefinition() {
		return definition;
	}

	@Override
	public int getResult(final long tableIndex) {
		try {
			if (tableIndex >= 0) {
				final ITable block = getBlockWithResult(tableIndex);
				
				return block.getResult(tableIndex);
			}
			else
				return TableResult.ILLEGAL;
		}
		catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Cannot read position result, ex");
		}
	}
	
}
