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

    private List<IParametricScalarField<Void>> getFieldsToTest() {
        return ImmutableList.of(
            new LinearScalarField(Vectors.of(2, -3)),
            PolynomialScalarField.parse(2,"3*x0^2 + x0*x1 + -5*x1^2"),
            new ScalarFieldWithStoredParameter<>(createLinearFeatureCombination(), getRandomSample(2)),
            createLinearRegressionCostField(),
            createLogisticRegressionCostField(),
            createRegularization()
        );
    }

    private IParametricScalarField<Void> createRegularization() {
        final Regularization regularization = new Regularization(2);
        regularization.addFeatures(ImmutableList.of(1));
        regularization.setLambda(0.34);

        return new ParameterMappingScalarField<>(regularization, x -> 5L);
    }

    private void testSingleFieldGradient(final IParametricScalarField<Void> field) {
        for (int i = 0; i < COUNT; i++) {
            final IVectorRead point = Vectors.getRandomVector(rng, field.getInputDimension());
            final ScalarPointCharacteristics scalarPointCharacteristics = field.calculate(point, null, ScalarFieldCharacteristic.SET_GRADIENT);

            for (int j = 0; j < field.getInputDimension(); j++) {
                final IVectorRead dPoint = Vectors.getUnitVector(j, field.getInputDimension()).multiply(EPSILON);
                final double d1 = field.calculateValue(point.minus(dPoint), null);
                final double d2 = field.calculateValue(point.plus(dPoint), null);
                final double expectedDerivation = (d2 - d1) / (2 * EPSILON);
                Assert.assertEquals(expectedDerivation, scalarPointCharacteristics.getGradient().getElement(j), MAX_DERIVATION_DIFF);
            }
        }
    }

    private void testSingleFieldHessian(final IParametricScalarField<Void> field) {
        for (int i = 0; i < COUNT; i++) {
            final IVectorRead point = Vectors.getRandomVector(rng, field.getInputDimension());
            final ScalarPointCharacteristics scalarPointCharacteristics = field.calculate(point, null, ScalarFieldCharacteristic.SET_HESSIAN);

            for (int j = 0; j < field.getInputDimension(); j++) {
                final IVectorRead dPoint = Vectors.getUnitVector(j, field.getInputDimension()).multiply(EPSILON);
                final IVectorRead d1 = field.calculateGradient(point.minus(dPoint), null);
                final IVectorRead d2 = field.calculateGradient(point.plus(dPoint), null);
                final IVectorRead expectedDerivation = d2.minus(d1).multiply(1.0 / (2 * EPSILON));
                Assert.assertEquals(
                        0.0,
                        expectedDerivation.minus(scalarPointCharacteristics.getHessian().getRowVector(j)).getLength(),
                        MAX_DERIVATION_DIFF
                );
            }
        }
    }

    private void testSingleFieldEqualFunctions(final IParametricScalarField<Void> field) {
        for (int i = 0; i < COUNT; i++) {
            final IVectorRead point = Vectors.getRandomVector(rng, field.getInputDimension());
            final ScalarPointCharacteristics scalarPointCharacteristics = field.calculate(point, null, ScalarFieldCharacteristic.SET_ALL);

            final double valueFromNonParametricFunction = field.calculateValue(point, null);
            final double valueFromParametricFunction = field.calculateValue(point, null);

            final IVectorRead gradientFromNonParametricFunction = field.calculateGradient(point, null);
            final IVectorRead gradientFromParametricFunction = field.calculateGradient(point, null);

            Assert.assertEquals(valueFromNonParametricFunction, valueFromParametricFunction, MAX_VALUE_DIFF);
            Assert.assertEquals(scalarPointCharacteristics.getValue(), valueFromParametricFunction, MAX_VALUE_DIFF);

            Assert.assertEquals(0.0, gradientFromNonParametricFunction.minus(gradientFromParametricFunction).getLength(), MAX_VALUE_DIFF);
            Assert.assertEquals(0.0, gradientFromNonParametricFunction.minus(scalarPointCharacteristics.getGradient()).getLength(), MAX_VALUE_DIFF);
        }
    }

    @Test
    public void testFieldGradients() {
        final List<IParametricScalarField<Void>> fields = getFieldsToTest();

        for (IParametricScalarField<Void> field: fields)
            testSingleFieldGradient(field);
    }

    @Test
    public void testFieldHessians() {
        final List<IParametricScalarField<Void>> fields = getFieldsToTest();

        for (IParametricScalarField<Void> field: fields)
            testSingleFieldHessian(field);
    }

    @Test
    public void testCompoundMethodSameAsSingles() {
        final List<IParametricScalarField<Void>> fields = getFieldsToTest();

        for (IParametricScalarField<Void> field: fields)
            testSingleFieldEqualFunctions(field);
    }

}
