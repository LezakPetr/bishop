package math;

import collections.DoubleArray;
import collections.IntArray;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

public class GaussJordanElimination<S> {
	private final int equationCount;
	private IntArray[] equationMatrixRowIndices;
	private IntArray[] equationMatrixColumnIndices;
	private DoubleArray[] equationMatrixElements;
	private final IMatrix rightSide;
	private final Function<GaussJordanElimination<S>, S> solutionExtractor;
	private final Integer[] columnOrder;

	private GaussJordanElimination (final IMatrixRead equationMatrix, final IMatrixRead rightSide, final Function<GaussJordanElimination<S>, S> solutionExtractor) {
		equationCount = equationMatrix.getRowCount();

		if (equationMatrix.getColumnCount() != equationCount)
			throw new RuntimeException("Matrix is not square matrix");

		if (rightSide.getRowCount() != equationCount)
			throw new RuntimeException("Right side has different number of rows");

		// Get elements of original matrix
		this.equationMatrixColumnIndices = new IntArray[equationCount];
		this.equationMatrixRowIndices = new IntArray[equationCount];
		this.equationMatrixElements = new DoubleArray[equationCount];

		copyEquationMatrix(equationMatrix);

		// Prepare inverse matrix - initialize it by identity matrix
		this.rightSide = rightSide.copy();

		columnOrder = IntStream.range(0, equationCount).mapToObj(i -> (Integer) i).toArray(Integer[]::new);
		Arrays.sort(columnOrder, (a, b) -> Integer.compare(equationMatrixRowIndices[a].getSize(), equationMatrixRowIndices[b].getSize()));

		this.solutionExtractor = Objects.requireNonNull(solutionExtractor);
	}

	private void copyEquationMatrix(IMatrixRead equationMatrix) {
		for (int i = 0; i < equationCount; i++)
			this.equationMatrixRowIndices[i] = new IntArray();

		for (int i = 0; i < equationCount; i++) {
			final IVectorRead row = equationMatrix.getRowVector(i);
			final int nonZeroElementCount = row.getNonZeroElementCount();

			this.equationMatrixColumnIndices[i] = new IntArray(nonZeroElementCount);
			this.equationMatrixElements[i] = new DoubleArray(nonZeroElementCount);

			for (IVectorIterator it = row.getNonZeroElementIterator(); it.isValid(); it.next()) {
				final int columnIndex = it.getIndex();
				equationMatrixColumnIndices[i].append(columnIndex);
				equationMatrixRowIndices[columnIndex].append(i);
				equationMatrixElements[i].append(it.getElement());
			}
		}
	}

	/**
	 * Returns gaussian elimination that calculates inverse matrix.
	 * @param matrix matrix to invert
	 */
	public static GaussJordanElimination<IMatrixRead> matrixInversion(final IMatrixRead matrix) {
		final int equationCount = matrix.getRowCount();

		return new GaussJordanElimination<>(matrix, Matrices.getIdentityMatrix(equationCount), g -> g.rightSide);
	}

	public static GaussJordanElimination<IVectorRead> equationSolver(final IMatrixRead matrix, final IVectorRead rightSide) {
		return new GaussJordanElimination<>(matrix, Matrices.fromColumnVector(rightSide), g -> g.rightSide.getColumnVector(0));
	}

	public static GaussJordanElimination<IMatrixRead> equationSolver(final IMatrixRead matrix, final IMatrixRead rightSide) {
		return new GaussJordanElimination<>(matrix, rightSide, g -> g.rightSide);
	}

	public S solve() {
		//for (int i = 0; i < equationCount; i++) {
		for (int i: columnOrder) {
			//assert checkIntegrity(i);

			normalizeDiagonalElement (i);

			final IntArray rowIndices = equationMatrixRowIndices[i];

			for (int j = 0; j < rowIndices.getSize(); j++) {
				final int eliminatedRowIndex = rowIndices.getItem(j);

				if (i != eliminatedRowIndex) {
					final double eliminatedElement = getElement(eliminatedRowIndex, i);

					if (eliminatedElement != 0.0) {
						combineRows (eliminatedRowIndex, i, -eliminatedElement, i);
					}
				}
			}
		}

		return solutionExtractor.apply(this);
	}

	private void combineRows (final int targetRowIndex, final int sourceRowIndex, final double sourceCoeff, final int eliminatedColumn) {
		final IntArray targetIndices = equationMatrixColumnIndices[targetRowIndex];
		final DoubleArray targetElements = equationMatrixElements[targetRowIndex];
		final IntArray sourceIndices = equationMatrixColumnIndices[sourceRowIndex];
		final DoubleArray sourceElements = equationMatrixElements[sourceRowIndex];

		final int maxLength = Math.min (targetIndices.getSize() + sourceIndices.getSize() - 1, equationCount) - 1;
		final IntArray resultIndices = new IntArray(maxLength);
		final DoubleArray resultElements = new DoubleArray(maxLength);

		int sourceSparseIndex = 0;
		int targetSparseIndex = 0;

		while (sourceSparseIndex < sourceIndices.getSize() && targetSparseIndex < targetIndices.getSize()) {
			final int sourceIndex = sourceIndices.getItem(sourceSparseIndex);
			final int targetIndex = targetIndices.getItem(targetSparseIndex);

			final double sourceElement;

			if (sourceIndex <= targetIndex) {
				sourceElement = sourceElements.getItem(sourceSparseIndex);
				sourceSparseIndex++;
			}
			else
				sourceElement = 0.0;

			final double targetElement;

			if (targetIndex <= sourceIndex) {
				targetElement = targetElements.getItem(targetSparseIndex);
				targetSparseIndex++;
			}
			else
				targetElement = 0.0;

			final int resultIndex = Math.min(sourceIndex, targetIndex);

			if (resultIndex != eliminatedColumn) {
				final double resultElement = sourceCoeff * sourceElement + targetElement;
				resultIndices.append(resultIndex);
				resultElements.append(resultElement);

				if (resultIndex != targetIndex)
					equationMatrixRowIndices[resultIndex].append(targetRowIndex);
			}
		}

		while (sourceSparseIndex < sourceIndices.getSize()) {
			final int resultIndex = sourceIndices.getItem(sourceSparseIndex);

			if (resultIndex != eliminatedColumn) {
				final double resultElement = sourceCoeff * sourceElements.getItem(sourceSparseIndex);
				resultIndices.append(resultIndex);
				resultElements.append(resultElement);
				equationMatrixRowIndices[resultIndex].append(targetRowIndex);
			}

			sourceSparseIndex++;
		}

		while (targetSparseIndex < targetIndices.getSize()) {
			final int resultIndex = targetIndices.getItem(targetSparseIndex);

			if (resultIndex != eliminatedColumn) {
				final double resultElement = targetElements.getItem(targetSparseIndex);
				resultIndices.append(resultIndex);
				resultElements.append(resultElement);
			}

			targetSparseIndex++;
		}

		equationMatrixColumnIndices[targetRowIndex] = resultIndices;
		equationMatrixElements[targetRowIndex] = resultElements;

		rightSide.setRowVector(
				targetRowIndex,
				rightSide.getRowVector(targetRowIndex)
					.multiplyAndAdd(rightSide.getRowVector(sourceRowIndex), sourceCoeff)
		);
	}

	private double getElement (final int rowIndex, final int columnIndex) {
		final IntArray columnIndices = equationMatrixColumnIndices[rowIndex];

		for (int i = 0; i < columnIndices.getSize(); i++) {
			if (columnIndices.getItem(i) == columnIndex)
				return equationMatrixElements[rowIndex].getItem(i);
		}

		return 0.0;
	}

	private void normalizeDiagonalElement (final int rowIndex) {
		final double coeff = 1.0 / getElement(rowIndex, rowIndex);
		final DoubleArray row = equationMatrixElements[rowIndex];

		for (int i = 0; i < row.getSize(); i++)
			row.setItem(i, row.getItem(i) * coeff);

		rightSide.setRowVector(rowIndex, rightSide.getRowVector(rowIndex).multiply(coeff));
	}

	private boolean checkIntegrity (final int minColumn) {
		final Set<String> s1 = new HashSet<>();

		for (int row = 0; row < equationCount; row++) {
			for (int i = 0; i < equationMatrixColumnIndices[row].getSize(); i++) {
				final int column = equationMatrixColumnIndices[row].getItem(i);

				if (column >= minColumn) {
					if (!s1.add(row + " " + column))
						return false;
				}
			}
		}

		final Set<String> s2 = new HashSet<>();

		for (int column = minColumn; column < equationCount; column++) {
			for (int i = 0; i < equationMatrixRowIndices[column].getSize(); i++) {
				if (!s2.add(equationMatrixRowIndices[column].getItem(i) + " " + column))
					return false;
			}
		}

		return s1.equals(s2);
	}

}
