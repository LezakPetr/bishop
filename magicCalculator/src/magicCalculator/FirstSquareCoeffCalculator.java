package magicCalculator;

import java.util.Arrays;
import java.util.SplittableRandom;

public class FirstSquareCoeffCalculator {

	private static final int BITS = 7;
	private static final int SIZE = 1 << BITS;

	public static void main (final String[] args) {
		final SplittableRandom rng = new SplittableRandom(12345);
		final boolean[] used = new boolean[SIZE];

		while (true) {
			final long coeff = rng.nextLong();

			Arrays.fill(used, false);
			used[0] = true;
			boolean ok = true;

			for (int i = 0; i < Long.SIZE; i++) {
				final int index = (int) ((coeff << i) >>> (Long.SIZE - BITS));

				if (used[index]) {
					ok = false;
					break;
				}

				used[index] = true;
			}

			if (ok) {
				System.out.println(coeff);
				break;
			}
		}


	}
}
