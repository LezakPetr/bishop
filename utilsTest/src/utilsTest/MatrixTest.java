package utilsTest;

import java.util.Random;
import java.util.function.BinaryOperator;

import org.junit.Assert;
import org.junit.Test;

import math.Density;
import math.IMatrix;
import math.IMatrixRead;
import math.Matrices;

public class MatrixTest {

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
		final IMatrixRead inv = Matrices.inverse(matrix);
		final IMatrixRead diff = Matrices.minus(inv, expected);
		
		Assert.assertTrue(Matrices.maxAbsElement (diff) <= 1e-6);
	}
	
	@Test
	public void inverseTest() {
		for (Density density: Density.values()) {
			initMatrices(density);
			
			checkInversion(matrixA, matrixInvA);
			checkInversion(matrixInvA, matrixA);
		}
	}
	
	// A * (B + C - D) = A*B + A*C - A*D
	@Test
	public void testCalculation() {
		for (int i = 0; i < 16; i++) {
			final IMatrix a = createRandomMatrix(getRandomDensity(), 2, 3);
			final IMatrix b = createRandomMatrix(getRandomDensity(), 3, 4);
			final IMatrix c = createRandomMatrix(getRandomDensity(), 3, 4);
			final IMatrix d = createRandomMatrix(getRandomDensity(), 3, 4);
			
			final IMatrixRead left = Matrices.multiply(a, Matrices.minus(Matrices.plus(b, c), d));
			final IMatrixRead right = Matrices.minus(Matrices.plus(Matrices.multiply(a, b), Matrices.multiply(a, c)), Matrices.multiply(a, d));
			
			Assert.assertEquals(left, right);
		}
	}
	
	@Test
	public void testAssociativity() {
		testAssociativityOfOperator(Matrices::plus);
		testAssociativityOfOperator(Matrices::multiply);
	}
	
	public void testAssociativityOfOperator(final BinaryOperator<IMatrixRead> operator) {
		for (int i = 0; i < 16; i++) {
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
		
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < columnCount; j++) {
				result.setElement(i, j, rng.nextInt(100) - 50);
			}
		}
		
		return result;
	}
	
	private Density getRandomDensity() {
		return (rng.nextBoolean()) ? Density.SPARSE : Density.DENSE;
	}
}
