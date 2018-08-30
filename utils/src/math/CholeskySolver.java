package math;

public class CholeskySolver {
	private final IMatrixRead matrix;
	private final IVectorRead rightSide;
	private final int equationCount;
	private final IMatrix triangle;
	private final IVector diagonal;

	public CholeskySolver (final IMatrixRead equationMatrix, final IVectorRead rightSide) {
		this.matrix = equationMatrix;
		this.rightSide = rightSide;
		this.equationCount = equationMatrix.getRowCount();
		this.triangle = Matrices.createMutableMatrix(equationMatrix.density(), equationCount, equationCount);
		this.diagonal = Vectors.vectorWithDensity(Density.DENSE, equationCount);

	}

	private double calculateElement (final int row, final int column) {
		return matrix.getElement(row, column) -
				Vectors.dotProduct(
						Vectors.elementMultiply(
								triangle.getRowVector(row).subVector(0, column),
								triangle.getRowVector(column).subVector(0, column)
						),
						diagonal.subVector(0, column)
				);
	}

	public void decomposition () {
		for (int i = 0; i < equationCount; i++) {
			final IVector triangleRow = triangle.getRowVector(i);

			for (int j = 0; j < i; j++) {
				final double value = calculateElement(i, j);

				if (value != 0)
					triangleRow.setElement(j, value / diagonal.getElement(j));
			}

			triangleRow.setElement(i, 1.0);
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
			final double subtractor = Vectors.dotProduct(
				triangle.getRowVector(i).subVector(0, i),
				result.subVector(0, i)
			);

			result.setElement(i, rightSide.getElement(i) - subtractor);
		}

		return result.freeze();
	}

	public IVectorRead solveBackwardTransposedTriangle(final IVectorRead previousRightSide) {
		final IMatrixRead transposedTriangle = Matrices.transpose(triangle);
		final IVector result = Vectors.dense(equationCount);

		for (int row = equationCount - 1; row >= 0; row--) {
			final double subtractor = Vectors.dotProduct(
					transposedTriangle.getRowVector(row).subVector(row + 1, equationCount),
					result.subVector(row + 1, equationCount)
			);

			result.setElement(row, previousRightSide.getElement(row) - subtractor);
		}

		return result.freeze();
	}

	public IVectorRead solve() {
		decomposition();

		final IVectorRead solution1 = solveBackwardTriangle();
		final IVectorRead solution2 = Vectors.elementDivide(solution1, diagonal);
		final IVectorRead solution3 = solveBackwardTransposedTriangle(solution2);

		return solution3;
	}

}
