package utilsTest;

import math.*;
import org.junit.Assert;
import org.junit.Test;

public class NewtonSolverTest {


    // (x, y) = (2, 3)
    // x + y - 5 = 0
    // x * y*y - 18 = 0
    public static NonLinearEquationSystemPoint equation (final IVectorRead input) {
        final double x = input.getElement(0);
        final double y = input.getElement(1);
        final IVectorRead output = Vectors.of(x + y - 5, x * y * y - 18);
        final IMatrix jacobian = Matrices.createMutableMatrix(Density.DENSE, 2, 2);
        jacobian.setElement(0, 0, 1);
        jacobian.setElement(0, 1, 1);
        jacobian.setElement(1, 0, y*y);
        jacobian.setElement(1, 1, 2*x*y);

        return new NonLinearEquationSystemPoint(
                input,
                output,
                jacobian.freeze()
        );
    }

    @Test
    public void testSolver() {
        final NewtonSolver solver = new NewtonSolver(2, NewtonSolverTest::equation);
        final IVectorRead solution = solver.solve();

        final IVectorRead output = equation(solution).getOutput();

        for (int i = 0; i < output.getDimension(); i++) {
            Assert.assertEquals(0, output.getElement(i), 1e-9);
        }
    }
}
