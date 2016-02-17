package bishopTests;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import bishop.base.BitBoard;
import bishop.base.Square;
import bishop.tablebase.GeneralCombinatorialNumberSystem;
import bishop.tablebase.ICombinatorialNumberSystem;
import bishop.tablebase.OneCombinatorialNumberSystem;
import bishop.tablebase.TwoCombinatorialNumberSystem;
import bishop.tablebase.ZeroCombinatorialNumberSystem;

public class CombinatorialNumberSystemTest {

	@Test
	public void reverseTest() {
		final Random rnd = new Random();
		
		for (int k = 1; k < 3; k++) {
			for (int n = k; n < Square.LAST; n++) {
				final ICombinatorialNumberSystem system = new GeneralCombinatorialNumberSystem(n, k);
				
				for (int i = 0; i < 100; i++) {
					final int[] items = new int[k];
					final long expectedMask = generateRandomItems(rnd, items, n, k);
					
					final int combinationIndex = system.getCombinationIndex(items);
					final long calculatedMask = system.getCombinationMask(combinationIndex);
					
					Assert.assertEquals(expectedMask, calculatedMask);
				}
			}
		}
	}

	public long generateRandomItems(final Random rnd, final int[] items, final int n, final int k) {
		long expectedMask = 0;
		
		for (int j = 0; j < k; j++) {
			int item;
			long mask;
			
			do {
				item = rnd.nextInt(n);
				mask = BitBoard.getSquareMask(item);
			} while ((expectedMask & mask) != 0);
			
			items[j] = item;
			expectedMask |= mask;
		}
		return expectedMask;
	}
	
	@Test
	public void sameAsGeneralTest() {
		for (int n = 1; n < Square.LAST; n++) {
			if (n >= 1)
				testSameAsGeneral (new OneCombinatorialNumberSystem(n));
			
			if (n >= 2)
				testSameAsGeneral (new TwoCombinatorialNumberSystem(n));
		}
	}

	private void testSameAsGeneral(final ICombinatorialNumberSystem specificSystem) {
		final Random rnd = new Random();
		
		final int n = specificSystem.getN();
		final int k = specificSystem.getK();
		final ICombinatorialNumberSystem generalSystem = new GeneralCombinatorialNumberSystem(n, k);
		
		for (int i = 0; i < 100; i++) {
			final int[] items = new int[k];
			final long expectedMask = generateRandomItems(rnd, items, n, k);
			
			final int specificCombinationIndex = specificSystem.getCombinationIndex(items);
			final int generalCombinationIndex = generalSystem.getCombinationIndex(items);
			Assert.assertEquals(generalCombinationIndex, specificCombinationIndex);
			
			final long calculatedMask = specificSystem.getCombinationMask(specificCombinationIndex);			
			Assert.assertEquals(expectedMask, calculatedMask);
		}
	}

}
