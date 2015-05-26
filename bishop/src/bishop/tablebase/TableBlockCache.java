package bishop.tablebase;

import java.util.HashMap;
import java.util.Map;

import utils.DoubleLinkedList;
import utils.ISimpleIterator;

public class TableBlockCache {
	
	private static class BlockRecord {
		private ITable block;
		private ISimpleIterator<BlockKey> historyIter;
		
		public ITable getBlock() {
			return block;
		}
		
		public void setBlock (final ITable block) {
			this.block = block;
		}
		
		public ISimpleIterator<BlockKey> getHistoryIter() {
			return historyIter;
		}
		
		public void setHistoryIter(final ISimpleIterator<BlockKey> iter) {
			this.historyIter = iter;
		}
	}
	
	private final int cacheSize;
	private final DoubleLinkedList<BlockKey> historyList;
	private final Map<BlockKey, BlockRecord> blockCache;
	
	public TableBlockCache(final int cacheSize) {
		this.cacheSize = cacheSize;
		this.historyList = new DoubleLinkedList<BlockKey>();
		this.blockCache = new HashMap<BlockKey, TableBlockCache.BlockRecord>();
	}
	
	public synchronized ITable getBlock (final BlockKey key) {
		final BlockRecord record = blockCache.get(key);
		
		if (record == null)
			return null;
	
		historyList.removeItem(record.getHistoryIter());
		
		final ISimpleIterator<BlockKey> newIter = historyList.addItemToEnd();
		newIter.setData(key);
		
		record.setHistoryIter(newIter);
		
		return record.getBlock();
	}
	
	public synchronized void addBlock (final BlockKey key, final ITable block) {
		if (blockCache.containsKey(key))
			return;

		final ISimpleIterator<BlockKey> newIter = historyList.addItemToEnd();
		newIter.setData(key);

		final BlockRecord record = new BlockRecord();
		record.setBlock(block);
		record.setHistoryIter(newIter);
		
		blockCache.put(key, record);
		
		if (blockCache.size() > cacheSize) {
			final ISimpleIterator<BlockKey> lastIter = historyList.getFirstItem();
			final BlockKey lastKey = lastIter.getData();
			
			historyList.removeItem(lastIter);
			blockCache.remove(lastKey);
		}
	}
}
