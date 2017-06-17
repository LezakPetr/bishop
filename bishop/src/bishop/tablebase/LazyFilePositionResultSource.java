package bishop.tablebase;

import java.io.File;
import java.lang.ref.SoftReference;

import bishop.base.IPosition;

/**
 * Table that creates FilePositionResultSource when it is necessary.
 * The FilePositionResultSource instance is held by soft reference.
 * 
 * @author Ing. Petr Ležák
 */
public class LazyFilePositionResultSource implements ITableRead {
	
	// Keep last used table reference to prevent collecting it
	@SuppressWarnings("unused")
	private static volatile FilePositionResultSource lastUsedTable;
	
	private final File file;
	private final TableBlockCache blockCache;
	private SoftReference<FilePositionResultSource> baseTableRef;
	
	public LazyFilePositionResultSource (final File file, final TableBlockCache blockCache) {
		this.file = file;
		this.blockCache = blockCache;
		this.baseTableRef = new SoftReference<>(null);   // The reference will not be null
	}
	
	private synchronized ITableRead getBaseTable() {
		FilePositionResultSource baseTable = baseTableRef.get();
		
		if (baseTable == null) {
			baseTable = new FilePositionResultSource(file, blockCache);
			baseTableRef = new SoftReference<>(baseTable);
		}
		
		lastUsedTable = baseTable;
		
		return baseTable;
	}
	
	@Override
	public int getPositionResult(final IPosition position) {
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
