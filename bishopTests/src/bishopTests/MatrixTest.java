package bishopTests;

import org.junit.Assert;
import org.junit.Test;

import math.IMatrix;
import math.Matrices;
import math.MatrixImpl;

public class MatrixTest {

	private double[][] elementsA = {{1.0, 2.0, 3.0}, {1.0, 4.0, 6.0}, {1.0, 0.0, -1.0}};
	private IMatrix matrixA = new MatrixImpl(elementsA);
	
	private double[][] elementsInvA = {{2.0, -1.0, 0.0}, {-3.5, 2.0, 1.5}, {2.0, -1.0, -1.0}};
	private IMatrix matrixInvA = new MatrixImpl(elementsInvA);
	
	
	private void checkInversion (final IMatrix matrix, final IMatrix expected) {
		final IMatrix inv = Matrices.inverse(matrix);
		final IMatrix diff = Matrices.minus(inv, expected);
		
		Assert.assertTrue(Matrices.maxAbsElement (diff) <= 1e-6);
	}
	
	@Test
	public void inverseTest() {
		checkInversion(matrixA, matrixInvA);
		checkInversion(matrixInvA, matrixA);
	}
	
}
