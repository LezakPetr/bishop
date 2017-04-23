package utilsTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import range.IProbabilityModel;
import range.ProbabilityModelFactory;
import range.RangeDecoder;
import range.RangeEncoder;
import utils.IoUtils;

public class RangeTest {
	
	private static final int SYMBOL_COUNT = 256;
	private static final int MIN_PROBABILITY = 1;
	private static final int MAX_PROBABILITY = 255;
	private static final int SEQUENCE_LENGTH = 1000000;
	private static final int PREWARM_COUNT = 10;

	
	private int[] getRandomArray (final Random rnd, final int count, final int minVal, final int maxVal) {
		final int[] array = new int[count];
		
		for (int i = 0; i < count; i++)
			array[i] = minVal + rnd.nextInt(maxVal-minVal);
		
		return array;
	}
	
	private long[] getRandomFrequencies(final Random rnd) {
		final int[] intProbabilities = getRandomArray (rnd, SYMBOL_COUNT, MIN_PROBABILITY, MAX_PROBABILITY);
		long[] frequencies = new long[SYMBOL_COUNT];
		
		for (int i = 0; i < SYMBOL_COUNT; i++)
			frequencies[i] = intProbabilities[i];
		
		return frequencies;
	}
	
	@Test
	public void correctnessTest() throws IOException {		
		final Random rnd = new Random(12345);
		final long[] frequencies = getRandomFrequencies(rnd);
		final int[] probabilities = ProbabilityModelFactory.normalizeProbabilities(frequencies);
		
		final IProbabilityModel model = ProbabilityModelFactory.fromProbabilities(probabilities);
		final int[] origSequence = getRandomArray (rnd, SEQUENCE_LENGTH, 0, SYMBOL_COUNT);
		final long[] lowArray = new long[SEQUENCE_LENGTH];
		final long[] highArray = new long[SEQUENCE_LENGTH];
		
		// Encode
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final RangeEncoder encoder = new RangeEncoder();
		
		encoder.initialize(outputStream);
		
		for (int i = 0; i < SEQUENCE_LENGTH; i++) {
			encoder.encodeSymbol(model, origSequence[i]);
			
			lowArray[i] = encoder.getLow();
			highArray[i] = encoder.getHigh();
		}
		
		encoder.close();
		
		// Decode
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		final RangeDecoder decoder = new RangeDecoder();
		
		decoder.initialize(inputStream);
		
		for (int i = 0; i < SEQUENCE_LENGTH; i++) {
			final int symbol = decoder.decodeSymbol(model);
			
			Assert.assertEquals(lowArray[i], decoder.getLow());
			Assert.assertEquals(highArray[i], decoder.getHigh());
			
			if (symbol != origSequence[i]) {
				Assert.fail();
			}
		}
		
		decoder.close();
		
		Assert.assertTrue("Not found end of stream", inputStream.read() < 0);
	}
	
	@Test
	public void speedTest() throws IOException {
		final Random rnd = new Random(12345);
		final long[] frequencies = getRandomFrequencies(rnd);
		final int[] probabilities = ProbabilityModelFactory.normalizeProbabilities(frequencies);
		
		final IProbabilityModel model = ProbabilityModelFactory.fromProbabilities (probabilities);
		final int[] origSequence = getRandomArray (rnd, SEQUENCE_LENGTH, 0, SYMBOL_COUNT);
		
		// Encode
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final RangeEncoder encoder = new RangeEncoder();
		
		for (int i = 0; i < PREWARM_COUNT; i++) {
			encodeSequence(encoder, model, origSequence, outputStream);
			outputStream.reset();
		}
		
		final long beginEncodeTime = System.currentTimeMillis();
		encodeSequence(encoder, model, origSequence, outputStream);
		final long endEncodeTime = System.currentTimeMillis();
		
		final long encodeTime = endEncodeTime - beginEncodeTime;
		
		System.out.println("Encode time " + encodeTime + "ms, " + (1000L * SEQUENCE_LENGTH / encodeTime) + " symbols/s");
		
		// Decode
		final RangeDecoder decoder = new RangeDecoder();

		for (int i = 0; i < PREWARM_COUNT; i++) {
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			decodeSequence(decoder, model, origSequence, inputStream);
		}
		
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		
		final long beginDecodeTime = System.currentTimeMillis();
		decodeSequence(decoder, model, origSequence, inputStream);		
		final long endDecodeTime = System.currentTimeMillis();
		
		final long decodeTime = endDecodeTime - beginDecodeTime;
		
		System.out.println("Decode time " + decodeTime + "ms, " + (1000L * SEQUENCE_LENGTH / decodeTime) + " symbols/s");		
	}

	private void encodeSequence(final RangeEncoder encoder, final IProbabilityModel model, final int[] sequence, final ByteArrayOutputStream outputStream) throws IOException {
		encoder.initialize(outputStream);
		
		for (int i = 0; i < SEQUENCE_LENGTH; i++) {
			encoder.encodeSymbol(model, sequence[i]);
		}
		
		encoder.close();
	}
	
	private void decodeSequence(final RangeDecoder decoder, final IProbabilityModel model, final int[] origSequence, final ByteArrayInputStream stream) throws IOException {
		decoder.initialize(stream);
		
		for (int i = 0; i < SEQUENCE_LENGTH; i++) {
			final int symbol = decoder.decodeSymbol(model);
			
			if (symbol != origSequence[i]) {
				Assert.fail();
			}
		}
		
		decoder.close();
	}


}
