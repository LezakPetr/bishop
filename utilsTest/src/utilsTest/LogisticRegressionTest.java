package utilsTest;

import math.IVectorRead;
import math.Vectors;
import org.junit.Assert;
import org.junit.Test;
import regression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogisticRegressionTest {
    private final Random rng = new Random(651664);

    private static final double M = 1.0/5.0;
    private static final double C0 = -5.0;
    private static final double C1 = -2.0;
    private static final double C2 = -3.0;
    private static final double C3 = 4.0;
    private static final double C4 = 2.5;
    private static final double C5 = -0.5;


    private static final long SAMPLE_COUNT = 100000;
    private static final double EPSILON = 0.2;

    private ISample getRandomSample() {
        final double x0 = random(-2, 2.5);
        final double x1 = random(-2, 3);
        final double z = M *(C0 + C1 * x0 + C2 * x1 + C3*x0*x0 + C4*x1*x1 + C5*x0*x1);
        final double probability = 1.0 / (1.0 + Math.exp(-z));

        final double rand = rng.nextDouble();
        final double y = (rand < probability) ? 1 : 0;

        return new Sample(Vectors.of(x0, x1), Vectors.of(y), 1.0);
    }

    private double random (final double from, final double to) {
        return rng.nextDouble() * (to - from) + from;
    }

    private List<ISample> getRandomSamples(final long count) {
        return Stream.generate(this::getRandomSample)
                .limit(count)
                .collect(Collectors.toList());
    }

    @Test
    public void testRegression() {
        final List<IScalarField> featureList = new ArrayList<>();
        featureList.add(PolynomialScalarField.parse(2, "1"));
        featureList.add(PolynomialScalarField.parse(2, "x0"));
        featureList.add(PolynomialScalarField.parse(2, "x1"));
        featureList.add(PolynomialScalarField.parse(2, "x0^2"));
        featureList.add(PolynomialScalarField.parse(2, "x1^2"));
        featureList.add(PolynomialScalarField.parse(2, "x0*x1"));

        final List<Integer> regularizedFeatures = new ArrayList<>();

        for (int i = 1; i < 6; i++)
            regularizedFeatures.add(i);


        final LogisticRegression regression = new LogisticRegression(2, 0, featureList, regularizedFeatures);

        regression.setMaxIterations(100);
        regression.setLambda (1e-3);

        final List<ISample> randomSamples = getRandomSamples(SAMPLE_COUNT);
        regression.addSamples (randomSamples);

        final IVectorRead optimum = regression.optimize().multiply(1.0 / M);
        final IVectorRead expected = Vectors.of(C0, C1, C2, C3, C4, C5);
        Assert.assertEquals(expected.getDimension(), optimum.getDimension());

        for (int i = 0; i < optimum.getDimension(); i++)
        System.out.println("O " + optimum.getElement(i));

        for (int i = 0; i < expected.getDimension(); i++)
            Assert.assertEquals(expected.getElement(i), optimum.getElement(i), EPSILON);
    }

    @Test
    public void testErrorCostFunction() {
        for (int y = 0; y <= 1; y++) {
            for (int z = -2; z <= 2; z++) {
                final double given = LogisticRegressionCostField.errorCostFunction(z, y);
                final double s = LogisticRegressionCostField.sigmoid(z);
                final double expected = (y == 0) ? -Math.log(1 - s) : -Math.log(s);

                Assert.assertEquals(expected, given, 1e-9);
            }
        }
    }
}
