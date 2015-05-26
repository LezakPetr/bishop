package bishopTests;

import org.junit.Assert;

import org.junit.Test;

import bishop.base.BitBoard;
import bishop.base.Square;

public class BitBoardTest {
	
	private long squareCountTest (final int bitShift, final int bitCount) {
		final long count = 1 << bitCount;
		long result = 0;
		
		for (long i = 0; i < count; i++) {
			result += BitBoard.getSquareCount(i << bitShift);
		}
		
		return result;
	}
	
	@Test
	public void testGetSquareCount() {
		final int BITS = 17;
		final long testedBoards = 1L << BITS;
		
		// In the tested range there is testedBoards boards and each has permuted BITS.
		// One half of permuted its is set, one half is clear.
		final long expectedCount = BITS * testedBoards / 2;

		for (int i = 0; i < Square.LAST - BITS; i++) {
			final long count = squareCountTest (i, BITS);
			
			Assert.assertEquals(expectedCount, count);
		}
	}
	
	
	
	@Test
	public void testGetSquareCountSpeed() {
		final int PREFETCH_BITS = 24;
		final int TEST_BITS = 30;
		
		// Prefetch
		squareCountTest (0, PREFETCH_BITS);
		
		// Test
		final long t1 = System.currentTimeMillis();
		squareCountTest (0, TEST_BITS);
		final long t2 = System.currentTimeMillis();
		
		final long callsPerSec = 1000 * (1L << TEST_BITS) / (t2 - t1);
		
		System.out.println (callsPerSec + " getSquareCount()/s");		
	}
}
