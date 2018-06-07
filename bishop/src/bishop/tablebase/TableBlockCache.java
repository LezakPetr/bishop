package bishop.tablebase;


import collections.RecordCache;

public class TableBlockCache extends RecordCache<BlockKey, ITable> {

	public TableBlockCache(final int cacheBits) {
		super(cacheBits);
	}

}
