package bishop.tablebase;

import java.util.HashMap;
import java.util.Map;

import utils.DoubleLinkedList;
import utils.ISimpleIterator;

public class TableBlockCache {
	
	private static class BlockRecord {
		private ITable block;
		private BlockKey blockKey;
		
		public ITable getBlock() {
			return block;
		}
		
		public void setBlock (final ITable block) {
			this.block = block;
		}
		
		public BlockKey getBlockKey() {
			return blockKey;
		}
		
		public void setBlockKey(final BlockKey blockKey) {
			this.blockKey = blockKey;
		}
	}
	
	private final int cacheMask;
	private final BlockRecord[] blockCache;
	
	public TableBlockCache(final int cacheBits) {
		final int cacheSize = 1 << cacheBits;
		this.blockCache = new BlockRecord[cacheSize];
		
		for (int i = 0; i < cacheSize; i++)
			this.blockCache[i] = new BlockRecord();
		
		this.cacheMask = cacheSize - 1;
	}
	
	private int getRecordIndex (final BlockKey blockKey) {
		return blockKey.hashCode() & cacheMask;
	}
	
	public ITable getBlock (final BlockKey key) {
		final int index = getRecordIndex(key);
		final BlockRecord record = blockCache[index];
		
		synchronized (record) {
			if (record.blockKey == null || !record.blockKey.equals(key))
				return null;
				
			return record.getBlock();
		}
	}
	
	public void addBlock (final BlockKey key, final ITable block) {
		final int index = getRecordIndex(key);
		final BlockRecord record = blockCache[index];
		
		synchronized (record) {
			record.setBlock(block);
			record.setBlockKey(key);
		}
	}
}
