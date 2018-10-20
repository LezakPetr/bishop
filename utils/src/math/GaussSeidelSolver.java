package math;

public class GaussSeidelSolver {
	private final int equationCount;
	private final IMatrixRead reducedEquationMatrix;
	private final IVectorRead diagonal;
	private final IVectorRead rightSide;
	private int maxIterations = 200;

	public GaussSeidelSolver(final IMatrixRead equationMatrix, final IVectorRead rightSide) {
		equationCount = equationMatrix.getRowCount();

		if (equationMatrix.getColumnCount() != equationCount)
			throw new RuntimeException("Matrix is not square matrix");

		if (rightSide.getDimension() != equationCount)
			throw new RuntimeException("Right side has different number of rows");

		final IMatrix reducedEquationMatrix = Matrices.createMutableMatrix(equationMatrix.density(), equationCount, equationCount);
		final IVector diagonal = Vectors.dense(equationCount);

		for (int rowIndex = 0; rowIndex < equationCount; rowIndex++) {
			for (IVectorIterator it = equationMatrix.getRowVector(rowIndex).getNonZeroElementIterator(); it.isValid(); it.next()) {
				final int columnIndex = it.getIndex();

				if (rowIndex == columnIndex)
					diagonal.setElement(rowIndex, it.getElement());
				else
					reducedEquationMatrix.setElement(rowIndex, columnIndex, it.getElement());
			}
		}

		this.reducedEquationMatrix = reducedEquationMatrix.freeze();
		this.diagonal = diagonal.freeze();
		this.rightSide = rightSide.immutableCopy();
	}

	public IVectorRead solve() {
		IVectorRead solution = rightSide;

		for (int i = 0; i < maxIterations; i++) {
			final IVectorRead prevSolution = solution;

			solution = rightSide
					.minus(reducedEquationMatrix.multiply(solution))
					.elementDivide(diagonal);

			final double diff = solution.minus(prevSolution).getLength();
			System.out.println (diff);

			if (diff < 1e-9)
				return solution;
		}

		throw new RuntimeException("Gauss Seidel solver is not converging");
	}
}
