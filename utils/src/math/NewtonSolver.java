package math;


import java.util.Random;

public class NewtonSolver {

    private final NonLinearEquationSystem equationSystem;
    private int maxIterations = 100;
    private IVectorRead input;

    public NewtonSolver (final int equationCount, final NonLinearEquationSystem equationSystem) {
        this.equationSystem = equationSystem;
        this.input = Vectors.getRandomVector(new Random(), equationCount);
    }

    public IVectorRead solve() {
        for (int i = 0; i < maxIterations; i++) {
            step();
        }

        return input;
    }

    private void step() {
        final NonLinearEquationSystemPoint point = equationSystem.apply(input);
        final IMatrixRead invJ = Matrices.inverse(point.getJacobian());
        input = Vectors.minus(input, Matrices.multiply(invJ, point.getOutput()));
    }

}
