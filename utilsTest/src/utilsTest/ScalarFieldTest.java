package utilsTest;

import collections.ImmutableList;
import math.IVectorRead;
import math.Vectors;
import org.junit.Assert;
import org.junit.Test;
import regression.*;

import java.util.List;
import java.util.Random;

public class ScalarFieldTest {

    private static final int COUNT = 100;
    private static final double EPSILON = 1e-6;
    private static final double MAX_DERIVATION_DIFF = 1e-6;
    private static final double MAX_VALUE_DIFF = 1e-14;

    private final Random rng = new Random(646546);

    private ISample getRandomSample(final int inputCount) {
        return new Sample(
                Vectors.getRandomVector(rng, inputCount),
                Vectors.getRandomVector(rng, 1),
                rng.nextDouble()
        );
    }

    private ISampleCostField createLinearFeatureCombination() {
        final LinearFeatureCombination field = new LinearFeatureCombination();
        field.addFeature(PolynomialScalarField.parse(2, "x0^2"));
        field.addFeature(PolynomialScalarField.parse(2, "x0*x1"));
        field.addFeature(PolynomialScalarField.parse(2, "x1^2"));

        return field;
    }

    private IScalarField createLinearRegressionCostField() {
        final LinearRegressionCostField field = new LinearRegressionCostField(
            3, 0,
                createLinearFeatureCombination()
        );

        return new ScalarFieldWithStoredParameter<>(
                field,
                getRandomSample(2)
        );
    }

    private IScalarField createLogisticRegressionCostField() {
        final LogisticRegressionCostField field = new LogisticRegressionCostField(
                3, 0,
                createLinearFeatureCombination()
        );

        return new ScalarFieldWithStoredParameter<>(
                field,
                getRandomSample(2)
        );
    }

    private List<IScalarField> getFieldsToTest() {
        return ImmutableList.of(
            PolynomialScalarField.parse(2,"3*x0^2 + x0*x1 + -5*x1^2"),
            new ScalarFieldWithStoredParameter<>(createLinearFeatureCombination(), getRandomSample(2)),
            createLinearRegressionCostField(),
            createLogisticRegressionCostField()
        );
    }

    private void testSingleFieldGradient(final IScalarField field) {
        for (int i = 0; i < COUNT; i++) {
            final IVectorRead point = Vectors.getRandomVector(rng, field.getInputDimension());
            final ScalarPointCharacteristics scalarPointCharacteristics = field.calculate(point, null, ScalarFieldCharacteristic.SET_GRADIENT);

            for (int j = 0; j < field.getInputDimension(); j++) {
                final IVectorRead dPoint = Vectors.multiply(EPSILON, Vectors.getUnitVector(j, field.getInputDimension()));
                final double d1 = field.calculateValue(Vectors.minus(point, dPoint));
                final double d2 = field.calculateValue(Vectors.plus(point, dPoint));
                final double expectedDerivation = (d2 - d1) / (2 * EPSILON);
                Assert.assertEquals(expectedDerivation, scalarPointCharacteristics.getGradient().getElement(j), MAX_DERIVATION_DIFF);
            }
        }
    }

    private void testSingleFieldHessian(final IScalarField field) {
        for (int i = 0; i < COUNT; i++) {
            final IVectorRead point = Vectors.getRandomVector(rng, field.getInputDimension());
            final ScalarPointCharacteristics scalarPointCharacteristics = field.calculate(point, null, ScalarFieldCharacteristic.SET_HESSIAN);

            for (int j = 0; j < field.getInputDimension(); j++) {
                final IVectorRead dPoint = Vectors.multiply(EPSILON, Vectors.getUnitVector(j, field.getInputDimension()));
                final IVectorRead d1 = field.calculateGradient(Vectors.minus(point, dPoint));
                final IVectorRead d2 = field.calculateGradient(Vectors.plus(point, dPoint));
                final IVectorRead expectedDerivation = Vectors.multiply(1.0 / (2 * EPSILON), Vectors.minus(d2, d1));
                Assert.assertEquals(
                        0.0,
                        Vectors.getLength(Vectors.minus(expectedDerivation, scalarPointCharacteristics.getHessian().getRowVector(j))),
                        MAX_DERIVATION_DIFF
                );
            }
        }
    }

    private void testSingleFieldEqualFunctions(final IScalarField field) {
        for (int i = 0; i < COUNT; i++) {
            final IVectorRead point = Vectors.getRandomVector(rng, field.getInputDimension());
            final ScalarPointCharacteristics scalarPointCharacteristics = field.calculate(point, null, ScalarFieldCharacteristic.SET_ALL);

            final double valueFromNonParametricFunction = field.calculateValue(point);
            final double valueFromParametricFunction = field.calculateValue(point, null);

            final IVectorRead gradientFromNonParametricFunction = field.calculateGradient(point);
            final IVectorRead gradientFromParametricFunction = field.calculateGradient(point, null);

            Assert.assertEquals(valueFromNonParametricFunction, valueFromParametricFunction, MAX_VALUE_DIFF);
            Assert.assertEquals(scalarPointCharacteristics.getValue(), valueFromParametricFunction, MAX_VALUE_DIFF);

            Assert.assertEquals(0.0, Vectors.getLength(Vectors.minus(gradientFromNonParametricFunction, gradientFromParametricFunction)), MAX_VALUE_DIFF);
            Assert.assertEquals(0.0, Vectors.getLength(Vectors.minus(gradientFromNonParametricFunction, scalarPointCharacteristics.getGradient())), MAX_VALUE_DIFF);
        }
    }

    @Test
    public void testFieldGradients() {
        final List<IScalarField> fields = getFieldsToTest();

        for (IScalarField field: fields)
            testSingleFieldGradient(field);
    }

    @Test
    public void testFieldHessians() {
        final List<IScalarField> fields = getFieldsToTest();

        for (IScalarField field: fields)
            testSingleFieldHessian(field);
    }

    @Test
    public void testCompoundMethodSameAsSingles() {
        final List<IScalarField> fields = getFieldsToTest();

        for (IScalarField field: fields)
            testSingleFieldEqualFunctions(field);
    }

}
