package bishopTests;

import bishop.base.Move;
import bishop.engine.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class BestMoveHashTableTest {

	private static class Data {
		private final long hash;
		private final int horizon;
		private final int compressedMove;
		
		public Data (final long hash, final int horizon, final int compressedMove) {
			this.hash = hash;
			this.horizon = horizon;
			this.compressedMove = compressedMove;
		}
	}
	
	@Test
	public void testHashTable() {
		final Random rnd = new Random();
		final int exponent = 8;
		final long mask = (1L << exponent) - 1;
		
		final BestMoveHashTableImpl hashTable = new BestMoveHashTableImpl(exponent);
		final Map<Integer, Data> expectedMap = new HashMap<>();
		
		for (int i = 0; i < 10000; i++) {
			final int compressedMove = rnd.nextInt(Move.LAST_COMPRESSED_MOVE);
			final int horizon = rnd.nextInt(ISearchEngine.MAX_HORIZON);
			final long hash = rnd.nextLong();
			hashTable.updateRecord(hash, horizon, compressedMove);

			final int index = (int) (hash & mask);
			final Data existingData = expectedMap.get(index);
			
			if (existingData == null || horizon >= existingData.horizon)
				expectedMap.put(index, new Data (hash, horizon, compressedMove));
		}
		
		for (Map.Entry<Integer, Data> entry: expectedMap.entrySet()) {
			final int compressedMove = hashTable.getRecord(entry.getValue().hash);
			
			Assert.assertEquals(entry.getValue().compressedMove, compressedMove);
		}
	}
}
