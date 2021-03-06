package utilsTest;

import math.*;
import org.junit.Assert;
import org.junit.Test;

public class CholeskySolverTest {
	@Test
	public void testDecomposition() {
		final IMatrixRead m = createTestMatrix();

		final CholeskySolver solver = new CholeskySolver(m, Vectors.getZeroVector(3));
		solver.decomposition();

		final IMatrixRead triangle = solver.getTriangle();
		final IVectorRead diagonal = solver.getDiagonal();

		final IMatrixRead recomposed = triangle
				.multiply(Matrices.getDiagonalMatrix(diagonal))
				.multiply(triangle.transpose());

		Assert.assertEquals(m, recomposed);
	}

	@Test
	public void testSolve() {
		final IMatrixRead m = createTestMatrix();
		final IVectorRead rightSide = Vectors.of(1.0, 2.0, 3.0);

		final CholeskySolver solver = new CholeskySolver(m, rightSide);
		final IVectorRead solution = solver.solve();
		final IVectorRead calculatedRightSide = m.multiply(solution);

		Assert.assertEquals(0.0, calculatedRightSide.minus(rightSide).getLength(), 1e-9);
	}

	private IMatrixRead createTestMatrix() {
		final IMatrix m = Matrices.createMutableMatrix(Density.SPARSE, 3, 3);
		m.setElement(0, 0, 1.0);
		m.setElement(0, 0, 2.0);
		m.setElement(0, 0, 3.0);
		m.setElement(1, 2, -1.0);
		m.setElement(2, 1, -1.0);
		m.setElement(0, 1, 4.0);
		m.setElement(1, 0, 4.0);

		return m.freeze();
	}
}
