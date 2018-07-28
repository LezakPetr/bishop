package regression;

import math.IVectorRead;
import math.Vectors;

/**
 * Optimizer based on gradient descend algorithm.
 */
public class GradientOptimizer<P> {

    private static final double MIN_ALPHA = 1e-6;
    private static final double MAX_ALPHA = 1e3;
    private static final double ALPHA_COEFF_BEFORE_OPTIMUM = 1.2;
    private static final double ALPHA_COEFF_AFTER_OPTIMUM = 0.8;
    private static final double ALPHA_COEFF_OUTPUT_INCREASED = 0.5;
    private static final double EPSILON = 1e-6;

    private final IParametricScalarField<P> field;
    private long maxIterations = 30000;
    private double alpha = 1e-2;
    private IVectorRead optimumInput;
    private ScalarPointCharacteristics optimumOutput;
    private double maxTerminationGradientLength;

    /**
     * Creates optimizer for given field.
     * @param field scalar field
     */
    public GradientOptimizer (final IParametricScalarField<P> field) {
        this.field = field;
    }

    /**
     * Initializes the optimizer.
     * @param x initial vector
     */
    public void initialize(final IVectorRead x) {
        if (x.getDimension() != field.getInputDimension())
            throw new RuntimeException("Dimensions does not match - vector: " + x.getDimension() + ", field: " + field.getInputDimension());

        this.optimumInput = Vectors.copy(x);
    }

    /**
     * Initializes the optimizer with zero initial vector.
     */
    public void initialize() {
        optimumInput = Vectors.getZeroVector(field.getInputDimension());
    }

    /**
     * Do the optimization.
     */
    public void optimize(final P parameter) {
        double currentAlpha = alpha;
        IVectorRead previousInput = optimumInput;
        ScalarPointCharacteristics previousOutput = field.calculate(previousInput, parameter, ScalarFieldCharacteristic.SET_VALUE_GRADIENT);
        optimumOutput = previousOutput;

        for (long i = 0; i < maxIterations; i++) {
            final IVectorRead previousGradient = previousOutput.getGradient();

            if (Vectors.getLength(previousGradient) <= maxTerminationGradientLength)
                break;

            final IVectorRead nextInput = Vectors.minus(
                    previousInput,
                    Vectors.multiply(currentAlpha, previousGradient)
            );

            final ScalarPointCharacteristics nextOutput = field.calculate(nextInput, parameter, ScalarFieldCharacteristic.SET_VALUE_GRADIENT);

            if (nextOutput.getValue() <= optimumOutput.getValue()) {
                optimumInput = nextInput;
                optimumOutput = nextOutput;
            }

            if (Vectors.dotProduct(previousGradient, nextOutput.getGradient()) >= 0)
                currentAlpha *= ALPHA_COEFF_BEFORE_OPTIMUM;
            else
                currentAlpha *= ALPHA_COEFF_AFTER_OPTIMUM;

            previousInput = nextInput;
            previousOutput = nextOutput;

            currentAlpha = Math.max(Math.min(currentAlpha, MAX_ALPHA), MIN_ALPHA);

            if (i % 100 == 0)
                System.out.println("val = " + optimumOutput.getValue() + "; alpha = " + currentAlpha);
        }
    }

    /**
     * Returns the optimum input.
     * @return optimum input
     */
    public IVectorRead getOptimumInput() {
        return optimumInput;
    }

    /**
     * Returns the optimum output.
     * @return optimum output
     */
    public double getOptimumOutput() {
        return optimumOutput.getValue();
    }

    public void setMaxIterations (final long count) {
        this.maxIterations = count;
    }

    public void setAlpha (final double alpha) {
        this.alpha = alpha;
    }

    public void setMaxTerminationGradientLength(final double length) {
        this.maxTerminationGradientLength = length;
    }
}
