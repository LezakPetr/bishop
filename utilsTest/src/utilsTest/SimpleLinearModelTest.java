package utilsTest;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import math.SimpleLinearModel;

public class SimpleLinearModelTest {
	
	private static final int NUMBER_SPREAD = 1000;
	private static final int TEST_COUNT = 1000;
	private static final int SAMPLE_COUNT = 100;

	final Random rng = new Random(1234);

	// Generates random even number; positive and negative
	// Having even numbers guarantees that reduceWeightOfSamples will have exact result.
	private int getRandomNumber() {
		return 2 * (rng.nextInt(2 * NUMBER_SPREAD + 1) - NUMBER_SPREAD);
	}
	
	@Test
	public void testExactMatch() {
		for (int i = 0; i < TEST_COUNT; i++) {
			final int a = getRandomNumber();
			final int b = getRandomNumber();
			
			final SimpleLinearModel model = new SimpleLinearModel();
			
			for (int j = 0; j < SAMPLE_COUNT; j++) {
				final int x = getRandomNumber();
				final int y = a*x + b;
				
				model.addSample(x, y);
			}
			
			model.recalculateModel();
			checkModel(model, a, b);
			
			model.reduceWeightOfSamples();
			model.recalculateModel();
			checkModel(model, a, b);
		}
	}

	private void checkModel(final SimpleLinearModel model, final int a, final int b) {
		for (int j = 0; j < SAMPLE_COUNT; j++) {
			final int x = getRandomNumber();
			final int y = a*x + b;
			final int yGiven = model.estimate(x);
			
			Assert.assertEquals(y, yGiven);
		}
	}
}
