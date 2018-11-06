package utilsTest;

import java.util.Random;
import java.util.function.BinaryOperator;

import math.*;
import org.junit.Assert;
import org.junit.Test;

public class MatrixTest {

	private static final int COUNT = 100;

	private final Random rng = new Random();
	private IMatrixRead matrixA;
	private IMatrixRead matrixInvA;
	
	public void initMatrices(final Density density) {
		matrixA = Matrices.createMutableMatrix(density, 3, 3)
			.setElement(0, 0, 1.0).setElement(0, 1, 2.0).setElement(0, 2, 3.0)
			.setElement(1, 0, 1.0).setElement(1, 1, 4.0).setElement(1, 2, 6.0)
			.setElement(2, 0, 1.0).setElement(2, 1, 0.0).setElement(2, 2, -1.0)
			.freeze();
		
		matrixInvA = Matrices.createMutableMatrix(density, 3, 3)
				.setElement(0, 0, 2.0).setElement(0, 1, -1.0).setElement(0, 2, 0.0)
				.setElement(1, 0, -3.5).setElement(1, 1, 2.0).setElement(1, 2, 1.5)
				.setElement(2, 0, 2.0).setElement(2, 1, -1.0).setElement(2, 2, -1.0)
				.freeze();
	}
	
	private void checkInversion (final IMatrixRead matrix, final IMatrixRead expected) {
		final IMatrixRead inv = GaussianElimination.matrixInversion(matrix).solve();
		final IMatrixRead diff = inv.minus(expected);
		
		Assert.assertTrue(diff.maxAbsElement() <= 1e-6);
	}
	
	@Test
	public void inverseTest() {
		for (Density density: Density.values()) {
			initMatrices(density);

			checkInversion(matrixA, matrixInvA);
			checkInversion(matrixInvA, matrixA);
		}
	}

	@Test
	public void equationSystemTestGaussianElimination() {
		for (Density density: Density.values()) {
			initMatrices(density);

			final IVectorRead r = Vectors.of (1.0, 2.0, -3.0);

			final IVectorRead s = GaussianElimination.equationSolver(matrixA, r).solve();
			Assert.assertEquals(0.0, matrixA.multiply(s).minus(r).getLength(), 1e-9);
		}
	}

	@Test
	public void equationSystemTestGaussJordanElimination() {
		for (Density density: Density.values()) {
			initMatrices(density);

			final IVectorRead r = Vectors.of (1.0, 2.0, -3.0);

			final IVectorRead s = GaussJordanElimination.equationSolver(matrixA, r).solve();
			Assert.assertEquals(0.0, matrixA.multiply(s).minus(r).getLength(), 1e-9);
		}
	}

	// Matrix x Matrix multiplication test
	// A * (B + C - D) = A*B + A*C - A*D
	@Test
	public void testCalculation1() {
		for (int i = 0; i < COUNT; i++) {
			final IMatrix a = createRandomMatrix(getRandomDensity(), 2, 3);
			final IMatrix b = createRandomMatrix(getRandomDensity(), 3, 4);
			final IMatrix c = createRandomMatrix(getRandomDensity(), 3, 4);
			final IMatrix d = createRandomMatrix(getRandomDensity(), 3, 4);
			
			final IMatrixRead left = a.multiply(b.plus(c).minus(d));
			final IMatrixRead right = a.multiply(b).plus(a.multiply(c)).minus(a.multiply(d));
			
			Assert.assertEquals(left, right);
		}
	}

	// Matrix x Vector multiplication test
	// (A * B) * V = A * (B * V) 
	@Test
	public void testCalculation2() {
		for (int i = 0; i < COUNT; i++) {
			final IMatrix a = createRandomMatrix(getRandomDensity(), 2, 3);
			final IMatrix b = createRandomMatrix(getRandomDensity(), 3, 4);
			final IVectorRead v = createRandomVector(getRandomDensity(), 4);
			
			final IVectorRead left = a.multiply(b).multiply(v);
			final IVectorRead right = a.multiply(b.multiply(v));
			
			Assert.assertEquals(left, right);
		}
	}
	
	// Vector x Matrix multiplication test
	// V * (A * B) = (V * A) * B 
	@Test
	public void testCalculation3() {
		for (int i = 0; i < COUNT; i++) {
			final IVectorRead v = createRandomVector(getRandomDensity(), 5);
			final IMatrix a = createRandomMatrix(getRandomDensity(), 5, 3);
			final IMatrix b = createRandomMatrix(getRandomDensity(), 3, 4);
			
			final IVectorRead left = v.multiply(a.multiply(b));
			final IVectorRead right = v.multiply(a).multiply(b);
			
			Assert.assertEquals(left, right);
		}
	}

	@Test
	public void testAssociativity() {
		testAssociativityOfOperator(IMatrixRead::plus);
		testAssociativityOfOperator(IMatrixRead::multiply);
	}
	
	public void testAssociativityOfOperator(final BinaryOperator<IMatrixRead> operator) {
		for (int i = 0; i < COUNT; i++) {
			final IMatrix a = createRandomMatrix(getRandomDensity(), 3, 3);
			final IMatrix b = createRandomMatrix(getRandomDensity(), 3, 3);
			final IMatrix c = createRandomMatrix(getRandomDensity(), 3, 3);
			
			final IMatrixRead left = operator.apply(a, operator.apply(b, c));
			final IMatrixRead right = operator.apply(operator.apply(a, b), c);
			
			Assert.assertEquals(left, right);
		}		
	}
	
	private IMatrix createRandomMatrix(final Density density, final int rowCount, final int columnCount) {
		final IMatrix result = Matrices.createMutableMatrix(density, rowCount, columnCount);

		if (rng.nextBoolean()) {
			for (int i = 0; i < rowCount; i++) {
				for (int j = 0; j < columnCount; j++) {
					result.setElement(i, j, getRandomElement());
				}
			}
		}
		
		return result;
	}

	private IVector createRandomVector(final Density density, final int dimension) {
		final IVector result = Vectors.vectorWithDensity(density, dimension);
		
		for (int i = 0; i < dimension; i++)
			result.setElement(i, getRandomElement());
		
		return result;
	}

	private int getRandomElement() {
		return rng.nextInt(100) - 50;
	}

	private Density getRandomDensity() {
		return (rng.nextBoolean()) ? Density.SPARSE : Density.DENSE;
	}
}
