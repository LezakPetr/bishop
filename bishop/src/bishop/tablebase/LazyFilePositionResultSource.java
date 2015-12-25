package bishop.tablebase;

import java.io.File;
import java.lang.ref.SoftReference;

import bishop.base.Position;

public class LazyFilePositionResultSource implements ITableRead {
	
	private static FilePositionResultSource lastUsedTable;   // Keep last used table reference to prevent collecting it
	
	private final File file;
	private final TableBlockCache blockCache;
	private SoftReference<FilePositionResultSource> baseTableRef;
	
	public LazyFilePositionResultSource (final File file, final TableBlockCache blockCache) {
		this.file = file;
		this.blockCache = blockCache;
		this.baseTableRef = new SoftReference<>(null);   // The reference will not be null
	}
	
	private ITableRead getBaseTable() {
		FilePositionResultSource baseTable = baseTableRef.get();
		
		if (baseTable == null) {
			baseTable = new FilePositionResultSource(file, blockCache);
			baseTableRef = new SoftReference<>(baseTable);
		}
		
		lastUsedTable = baseTable;
		
		return baseTable;
	}
	
	@Override
	public int getPositionResult(final Position position) {
		return getBaseTable().getPositionResult(position);
	}

	@Override
	public ITableIteratorRead getIterator() {
		return getBaseTable().getIterator();
	}

	@Override
	public TableDefinition getDefinition() {
		return getBaseTable().getDefinition();
	}

	@Override
	public int getResult(final long index) {
		return getBaseTable().getResult(index);
	}
	
}
