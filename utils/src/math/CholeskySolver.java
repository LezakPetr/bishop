package math;

import utils.IntUtils;

public class CholeskySolver {
	private final IMatrixRead matrix;
	private final IVectorRead rightSide;
	private final int equationCount;
	private final IMatrix triangle;
	private final IVector diagonal;

	// Arrays with masks. Each item corresponds to one row of triangle.
	// Every bit is set if there is at least 1 non-zero element in the 1/64th part of the triangle.
	private final int indexShift;
	private final long[] nonZeroIndexMasks;

	public CholeskySolver (final IMatrixRead equationMatrix, final IVectorRead rightSide) {
		this.matrix = equationMatrix;
		this.rightSide = rightSide;
		this.equationCount = equationMatrix.getRowCount();
		this.triangle = Matrices.createMutableMatrix(equationMatrix.density(), equationCount, equationCount);
		this.diagonal = Vectors.vectorWithDensity(Density.DENSE, equationCount);
		this.indexShift = Math.max(IntUtils.ceilLog(equationCount) - 6, 0);
		this.nonZeroIndexMasks = new long[equationCount];
	}

	private double calculateElement (final int row, final int column) {
		final long mask = IntUtils.getLowestBitsLong((column >> indexShift) + 1);
		final double product;

		if ((nonZeroIndexMasks[row] & nonZeroIndexMasks[column] & mask) == 0)
			product = 0.0;
		else
			product = triangle.getRowVector(row).subVector(0, column)
			          .elementMultiply(triangle.getRowVector(column).subVector(0, column))
			          .dotProduct(diagonal.subVector(0, column));

		return matrix.getElement(row, column) - product;

	}

	public void decomposition () {
		for (int i = 0; i < equationCount; i++) {
			final IVector triangleRow = triangle.getRowVector(i);

			for (int j = 0; j < i; j++) {
				final double value = calculateElement(i, j);

				if (value != 0) {
					triangleRow.setElement(j, value / diagonal.getElement(j));
					nonZeroIndexMasks[i] |= 1L << (j >> indexShift);
				}
			}

			triangleRow.setElement(i, 1.0);
			nonZeroIndexMasks[i] |= 1L << (i >> indexShift);

			diagonal.setElement(i, calculateElement(i, i));
		}
	}

	public IMatrixRead getTriangle() {
		return triangle;
	}

	public IVectorRead getDiagonal() {
		return diagonal;
	}

	public IVectorRead solveBackwardTriangle() {
		final IVector result = Vectors.dense(equationCount);

		for (int i = 0; i < equationCount; i++) {
			final double subtractor = triangle.getRowVector(i).subVector(0, i)
					.dotProduct(result.subVector(0, i));

			result.setElement(i, rightSide.getElement(i) - subtractor);
		}

		return result.freeze();
	}

	public IVectorRead solveBackwardTransposedTriangle(final IVectorRead previousRightSide) {
		final IMatrixRead transposedTriangle = triangle.transpose();
		final IVector result = Vectors.dense(equationCount);

		for (int row = equationCount - 1; row >= 0; row--) {
			final double subtractor = transposedTriangle.getRowVector(row).subVector(row + 1, equationCount)
					.dotProduct(result.subVector(row + 1, equationCount));

			result.setElement(row, previousRightSide.getElement(row) - subtractor);
		}

		return result.freeze();
	}

	public IVectorRead solve() {
		decomposition();

		final IVectorRead solution1 = solveBackwardTriangle();
		final IVectorRead solution2 = solution1.elementDivide(diagonal);
		final IVectorRead solution3 = solveBackwardTransposedTriangle(solution2);

		return solution3;
	}

}
