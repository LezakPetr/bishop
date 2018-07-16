package regression;



public class LogisticRegressionCostField extends SampleCostFieldImpl {
    public LogisticRegressionCostField(final int inputDimension, final int outputIndex, final IParametricScalarField<ISample> valueField) {
        super (
            inputDimension,
            valueField,
            LogisticRegressionCostField::errorCostFunction,
            LogisticRegressionCostField::derivation,
            outputIndex
        );
    }

    public static double sigmoid (final double z) {
        return 0.5 * (1.0 + Math.tanh(0.5*z));
    }

    public static double errorCostFunction (final double z, final double y) {
        return z * (1 - y) + psi(z);
    }

    public static double psi (final double z) {
        if (z >= 0)
            return Math.log1p(Math.exp(-z));
        else
            return Math.log1p(Math.exp(z)) - z;
    }

    public static double derivation (final double z, final double y) {
        final double value = sigmoid(z);

        return value - y;
    }

}
