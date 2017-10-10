package utilsTest;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.LongBinaryOperator;
import java.util.stream.IntStream;

import org.junit.Test;

import org.junit.Assert;
import utils.Mixer;

public class MixerTest {

	private static final int TEST_COUNT = 1000000;
	
	public static long getInverseCoeff(final long coeff) {
		final BigInteger coeffBig = BigInteger.valueOf(coeff);
		final BigInteger modulus = BigInteger.ONE.shiftLeft(Long.SIZE);
		
		return coeffBig.modInverse(modulus).longValue();
	}
	
	@Test
	public void testInversibility() {
		final Random rng = new Random();
		final long inverseCoeff1 = getInverseCoeff(Mixer.MIX_COEFF1);
		final long inverseCoeff2 = getInverseCoeff(Mixer.MIX_COEFF2);
		final long inverseXor = Mixer.MIX_CONSTANT_XOR ^ (Mixer.MIX_CONSTANT_XOR >>> 32);
		
		for (int i = 0; i < TEST_COUNT; i++) {
			final long input = rng.nextLong();
			final long mixed = Mixer.mixLong(input);
			final long reversed = Mixer.mixLongWithCoeffs(mixed, inverseCoeff2, inverseCoeff1, inverseXor);
			
			Assert.assertEquals(input, reversed);
		}
	}
	
	public void testNonlinearityWithOperation (final LongBinaryOperator op) {
		final Random rng = new Random();
		int linearCount = 0;
		
		for (int i = 0; i < TEST_COUNT; i++) {
			final long a = rng.nextLong();
			final long b = rng.nextLong();
			final long result1 = Mixer.mixLong(op.applyAsLong(a, b));
			final long result2 = op.applyAsLong(Mixer.mixLong(a), Mixer.mixLong(b));
			
			if (result1 == result2)
				linearCount++;
		}
		
		Assert.assertTrue(linearCount < TEST_COUNT / 1000);
	}
	
	@Test
	public void testNonlinearity() {
		testNonlinearityWithOperation((a, b) -> a + b);
		testNonlinearityWithOperation((a, b) -> a ^ b);
	}
	
	private static final int COUNTER_BITS = 16;
	private static final int COUNTER_SIZE = 1 << COUNTER_BITS;
	private static final int MIN_OUTPUT_SET_SIZE = 41240;   // calculateExpectedOutputSetSize(COUNTER_SIZE)
	
	@Test
	public void testDifussion() {		
		for (int inputOffset = 0; inputOffset < Long.SIZE; inputOffset += COUNTER_BITS) {
			for (int outputOffset = 0; outputOffset < Long.SIZE; outputOffset += COUNTER_BITS) {
				final Set<Long> outputSet = new HashSet<>();
				
				for (long i = 0; i < COUNTER_SIZE; i++) {
					final long input = i << inputOffset;
					final long output = Mixer.mixLong(input);
					final long reducedOutput = (output >>> outputOffset) & (COUNTER_SIZE - 1);
					
					outputSet.add(reducedOutput);
				}
				
				System.out.println(outputSet.size());
				Assert.assertTrue(outputSet.size() > MIN_OUTPUT_SET_SIZE);
			}
		}
	}
	
	private static int calculateExpectedOutputSetSize(final int counterSize) {
		final double[] coeffs = IntStream.rangeClosed(0, counterSize).mapToDouble(x -> (double) x / (double) counterSize).toArray();
		double[] actual = new double[counterSize + 1];   // Probability, that there is index items in the set
		actual[0] = 1.0;
		
		for (int i = 0; i < counterSize; i++) {
			for (int j = i + 1; j >= 0; j--) {
				double updated = actual[j] * coeffs[j];
				
				if (j > 0)
					updated += actual[j - 1] * (1.0 - coeffs[j - 1]);
				
				actual[j] = updated;
			}
		}
		
		// Find 1% quantil
		double sum = 0.0;
		
		for (int i = 0; i < counterSize; i++) {
			sum += actual[i];
			
			if (sum > 0.01)
				return i - 1;
		}
		
		return counterSize;
	}
}
