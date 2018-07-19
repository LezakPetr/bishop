package math;


import java.util.Random;

public class NewtonSolver {

    private final NonLinearEquationSystem equationSystem;
    private long maxIterations = 100;
    private IVectorRead input;
    private double epsilon = 1e-9;

    public NewtonSolver (final int equationCount, final NonLinearEquationSystem equationSystem) {
        this.equationSystem = equationSystem;
        this.input = Vectors.getRandomVector(new Random(), equationCount);
    }

    public IVectorRead solve() {
        NonLinearEquationSystemPoint previousPoint = equationSystem.apply(input);
        double previousResiduum = Vectors.getLength(previousPoint.getOutput());
        int omegaExponent = 0;

        for (int i = 0; i < maxIterations; i++) {
            final IMatrixRead invJ = Matrices.inverse(previousPoint.getJacobian());
            final double omega = Math.pow(2, omegaExponent);

            final IVectorRead nextInput = Vectors.minus(
                    input,
                    Vectors.multiply(omega, Matrices.multiply(invJ, previousPoint.getOutput()))
            );

            final NonLinearEquationSystemPoint nextPoint = equationSystem.apply(nextInput);
            final double nextResiduum = Vectors.getLength(nextPoint.getOutput());

            System.out.println ("Iteration = " + i + ", omega = " + omega + ", residuum = " + nextResiduum);

            if (nextResiduum <= previousResiduum) {
                input = nextInput;
                previousPoint = nextPoint;
                previousResiduum = nextResiduum;
                omegaExponent = Math.min(omegaExponent + 1, 0);
            }
            else
                omegaExponent--;

            if (nextResiduum <= epsilon)
                return input;
        }

        return null;
    }

    public void setMaxIterations(final long count) {
        this.maxIterations = count;
    }

}
