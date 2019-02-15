package bishopTests;

import math.OnlineLogisticModel;
import math.Utils;
import org.junit.Assert;
import org.junit.Test;
import regression.LogisticRegressionCostField;

import java.util.SplittableRandom;

public class OnlineLogisticModelTest {
	@Test
	public void testConvergence() {
		final SplittableRandom rng = new SplittableRandom(1234);
		final OnlineLogisticModel model = new OnlineLogisticModel();

		final int count = 1000000;
		final double slope = 2.0;
		final double intercept = -1.0;
		double totalSquareError = 0.0;

		for (int i = 0; i < count; i++) {
			final double x = rng.nextDouble();
			final double y = LogisticRegressionCostField.sigmoid(intercept + x * slope);

			if (i < count / 2)
				model.addSample(x, (y > rng.nextDouble()) ? 1 : 0);
			else
				totalSquareError += Utils.sqr(model.getProbability(x) - y);
		}

		final double meanError = totalSquareError / (count / 2);
		System.out.println("Mean error " + meanError);

		Assert.assertTrue(meanError < 2e-3);
	}
}
