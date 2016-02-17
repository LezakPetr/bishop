package bishopTests;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import bishop.base.Move;
import bishop.engine.Evaluation;
import bishop.engine.HashRecord;
import bishop.engine.HashRecordType;
import bishop.engine.HashTableImpl;
import bishop.engine.ISearchEngine;


public class HashTableTest {
	
	private HashRecord generateRecord (final Random rnd) {
		final HashRecord record = new HashRecord();
		record.setCompressedBestMove(rnd.nextInt(Move.LAST_COMPRESSED_MOVE));
		record.setEvaluation(Evaluation.MIN + rnd.nextInt(2 * Evaluation.MAX));
		record.setHorizon(rnd.nextInt(ISearchEngine.MAX_HORIZON));
		record.setType(rnd.nextInt(HashRecordType.LAST));
		
		return record;
	}
	
	@Test
	public void testHashTable() {
		final Random rnd = new Random();
		final int exponent = 8;
		
		// Table uses exponent bits starting on bit 1, this is why we need two times bigger table
		final HashTableImpl hashTable = new HashTableImpl(2 * exponent);
		final Map<Long, HashRecord> expectedMap = new HashMap<>();
		
		for (int i = 0; i < 10000; i++) {
			final HashRecord record = generateRecord(rnd);
			final long hash = 2 * rnd.nextInt(1 << exponent);
			hashTable.updateRecord(hash, record);
			
			final HashRecord existingRecord = expectedMap.get(hash);
			
			if (existingRecord == null || record.getHorizon() >= existingRecord.getHorizon())
				expectedMap.put(hash, record);
		}
		
		for (Map.Entry<Long, HashRecord> entry: expectedMap.entrySet()) {
			final HashRecord record = new HashRecord();
			hashTable.getRecord(entry.getKey(), record);
			
			Assert.assertEquals(entry.getValue(), record);
		}
	}
}
