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
		final OnlineLogisticModel model = new OnlineLogisticModel(2);

		final int count = 2000000;
		final double slope0 = 2.0;
		final double slope1 = -0.5;
		final double intercept = -1.0;
		double totalSquareError = 0.0;

		for (int i = 0; i < count; i++) {
			final double x0 = rng.nextDouble();
			final double x1 = rng.nextDouble();
			final double y = LogisticRegressionCostField.sigmoid(intercept + x0 * slope0 + x1 * slope1);

			if (i < count / 2)
				model.addSample(new int[] {0, 1}, new double[] {x0, x1}, (y > rng.nextDouble()) ? 1 : 0);
			else
				totalSquareError += Utils.sqr(model.getProbability(new int[] {0, 1}, new double[] {x0, x1}) - y);
		}

		final double meanError = totalSquareError / (count / 2);
		System.out.println("Mean error " + meanError);

		Assert.assertTrue(meanError < 2e-3);
	}
}
