package bishopTests;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import bishop.base.Move;
import bishop.engine.HashRecord;
import bishop.engine.HashRecordType;
import bishop.engine.EvaluationHashTableImpl;
import bishop.engine.ISearchEngine;


public class EvaluationHashTableTest {
	
	private HashRecord generateRecord (final Random rnd) {
		final HashRecord record = new HashRecord();
		record.setEvaluation(HashRecord.MIN_NORMAL_EVALUATION + rnd.nextInt(2 * HashRecord.MAX_NORMAL_EVALUATION));
		record.setHorizon(rnd.nextInt(ISearchEngine.MAX_HORIZON));
		record.setType(rnd.nextInt(HashRecordType.LAST));
		
		return record;
	}
	
	private static class Data {
		public final long hash;
		public final HashRecord record;
		
		public Data (final long hash, final HashRecord record) {
			this.hash = hash;
			this.record = record;
		}
	}
	
	@Test
	public void testHashTable() {
		final Random rnd = new Random();
		final int exponent = 8;
		final long mask = (1L << exponent) - 1;
		
		final EvaluationHashTableImpl hashTable = new EvaluationHashTableImpl(exponent);
		final Map<Integer, Data> expectedMap = new HashMap<>();
		
		for (int i = 0; i < 10000; i++) {
			final HashRecord record = generateRecord(rnd);
			final long hash = rnd.nextLong();
			hashTable.updateRecord(hash, record);

			final int horizon = record.getHorizon();
			final int index = (int) ((hash + horizon) & mask);
			final Data existingData = expectedMap.get(index);
			
			if (existingData == null || horizon >= existingData.record.getHorizon())
				expectedMap.put(index, new Data (hash, record));
		}
		
		for (Map.Entry<Integer, Data> entry: expectedMap.entrySet()) {
			final HashRecord record = new HashRecord();
			hashTable.getRecord(entry.getValue().hash, entry.getValue().record.getHorizon(), record);
			
			Assert.assertEquals(entry.getValue().record, record);
		}
	}
}
