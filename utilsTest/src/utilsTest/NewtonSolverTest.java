package utilsTest;

import math.IVectorRead;
import org.junit.Assert;
import org.junit.Test;
import regression.NewtonSolver;
import regression.PolynomialScalarField;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NewtonSolverTest {

    private void testField (final String ...fieldStr) {
        final int inputDimension = fieldStr.length;
        final List<PolynomialScalarField> fields = Arrays.stream(fieldStr)
                .map(s -> PolynomialScalarField.parse(inputDimension, s))
                .collect(Collectors.toList());

        PolynomialScalarField costField = PolynomialScalarField.of(inputDimension, Collections.emptyList());

        for (PolynomialScalarField field: fields)
            costField = costField.add (field.multiply(field));

        final NewtonSolver solver = new NewtonSolver(inputDimension, costField);
        solver.setEpsilon (1e-12);

        final IVectorRead solution = solver.solve();

        for (PolynomialScalarField field: fields) {
            Assert.assertEquals(0, field.apply(solution), 1e-5);
        }
    }

    @Test
    public void testSolver() {
        testField ("x0 + -3");
        testField ("x0 + 3*x1 + -3", "5*x0 + -2*x1 + 3");
        testField ("x0^2", "x1^2");
    }
}
