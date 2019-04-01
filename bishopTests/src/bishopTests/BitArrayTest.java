package bishopTests;

import bishop.tablebase.BitArray;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.SplittableRandom;

public class BitArrayTest {
	@Test
	public void test() {
		final SplittableRandom rng = new SplittableRandom(1234);

		for (int i = 0; i < 1000; i++) {
			// Fill BitArray with random data
			final int size = rng.nextInt(1024);
			final BitArray bitArray = new BitArray(size);
			Assert.assertEquals(size, bitArray.getSize());

			final Set<Integer> expected = new HashSet<>();

			for (int j = 0; j < size; j++) {
				final int index = rng.nextInt(size);

				if (rng.nextBoolean()) {
					bitArray.setAt(index, true);
					expected.add(index);
				} else {
					bitArray.setAt(index, false);
					expected.remove(index);
				}
			}

			// Check content of BitArray
			final Set<Integer> given = new HashSet<>();

			for (int j = 0; j < size; j++) {
				if (bitArray.getAt(j))
					given.add(j);
			}

			Assert.assertEquals(expected, given);
		}
	}
}
