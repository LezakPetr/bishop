package math;

public class NonLinearEquationSystemPoint {
    private final IVectorRead input;
    private final IVectorRead output;
    private final IMatrixRead jacobian;

    public NonLinearEquationSystemPoint(final IVectorRead input, final IVectorRead output, final IMatrixRead jacobian) {
        this.input = input;
        this.output = output;
        this.jacobian = jacobian;
    }

    public IVectorRead getInput() {
        return input;
    }

    public IVectorRead getOutput() {
        return output;
    }

    public IMatrixRead getJacobian() {
        return jacobian;
    }
}
