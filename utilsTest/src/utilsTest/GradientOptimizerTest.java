package utilsTest;

import math.IVectorRead;
import math.Vectors;
import org.junit.Assert;
import org.junit.Test;
import regression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GradientOptimizerTest {
    private static final double X0_OPT = 2;
    private static final double X1_OPT = 3;

    private static final int SAMPLE_COUNT = 100;

    private IScalarField field = PolynomialScalarField.parse(
            2,
            "x0^2 + -4*x0 + 4 + x1^2 + -6*x1 + 9"
    );

    @Test
    public void testConvergenceFunction() {
        final GradientOptimizer<Void> optimizer = new GradientOptimizer<>(field);
        optimizer.initialize();
        optimizer.optimize(null);

        verifyOptimum(optimizer);
    }

    private void verifyOptimum(GradientOptimizer optimizer) {
        final IVectorRead optimumInput = optimizer.getOptimumInput();
        final double optimumOutput = optimizer.getOptimumOutput();

        Assert.assertEquals(2, optimumInput.getDimension());
        Assert.assertEquals(X0_OPT, optimumInput.getElement(0), 1e-3);
        Assert.assertEquals(X1_OPT, optimumInput.getElement(1), 1e-3);
        Assert.assertEquals(0.0, optimumOutput, 1e-3);
    }

    @Test
    public void testConvergenceSamples() {
        final List<ISample> samples = new ArrayList<>();
        final Random rng = new Random(1234);

        final LinearFeatureCombination valueField = new LinearFeatureCombination();
        valueField.addFeature(PolynomialScalarField.parse(1, "1"));
        valueField.addFeature(PolynomialScalarField.parse(1, "x0"));

        final IVectorRead expectedOptimum = Vectors.of(5, -2);

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            final double x = 20 * rng.nextDouble() - 10;
            final ISample sample = new Sample(Vectors.of(x), Vectors.of(0.0), 1.0);

            final double y = valueField.calculateValue(expectedOptimum, sample);
            samples.add(new Sample(Vectors.of(x), Vectors.of(y), 1.0));
        }

        final ISampleCostField sampleCostField = new LinearRegressionCostField(2, 0, valueField);
        final MultiSampleCostField multiSampleCostField = new MultiSampleCostField(sampleCostField);
        multiSampleCostField.addSamples(samples);

        final GradientOptimizer optimizer = new GradientOptimizer(multiSampleCostField);
        optimizer.initialize();
        optimizer.optimize(null);

        final double diff = expectedOptimum.minus(optimizer.getOptimumInput()).getLength();
        Assert.assertEquals(0.0, diff, 1e-6);
    }


}
