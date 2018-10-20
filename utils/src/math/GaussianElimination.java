package math;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class GaussianElimination<S> {

    private final int equationCount;
    private final IMatrix equationMatrix;
    private final IMatrix rightSide;
    private final Function<GaussianElimination<S>, S> solutionExtractor;

    private GaussianElimination (final IMatrixRead equationMatrix, final IMatrixRead rightSide, final Function<GaussianElimination<S>, S> solutionExtractor) {
        equationCount = equationMatrix.getRowCount();

        System.out.println (equationMatrix.getRowVector(0).density());
        System.out.println (IntStream.range(0, equationCount).map(i -> equationMatrix.getRowVector(i).getNonZeroElementCount()).average().getAsDouble());

        if (equationMatrix.getColumnCount() != equationCount)
            throw new RuntimeException("Matrix is not square matrix");

        if (rightSide.getRowCount() != equationCount)
            throw new RuntimeException("Right side has different number of rows");

        // Get elements of original matrix
        this.equationMatrix = equationMatrix.copy();

        // Prepare inverse matrix - initialize it by identity matrix
        this.rightSide = rightSide.copy();

        this.solutionExtractor = Objects.requireNonNull(solutionExtractor);
    }

    /**
     * Returns gaussian elimination that calculates inverse matrix.
     * @param matrix matrix to invert
     */
    public static GaussianElimination<IMatrixRead> matrixInversion(final IMatrixRead matrix) {
        final int equationCount = matrix.getRowCount();

        return new GaussianElimination<>(matrix, Matrices.getIdentityMatrix(equationCount), g -> g.rightSide);
    }

    public static GaussianElimination<IVectorRead> equationSolver(final IMatrixRead matrix, final IVectorRead rightSide) {
        return new GaussianElimination<>(matrix, Matrices.fromColumnVector(rightSide), g -> g.rightSide.getColumnVector(0));
    }

    public static GaussianElimination<IMatrixRead> equationSolver(final IMatrixRead matrix, final IMatrixRead rightSide) {
        return new GaussianElimination<>(matrix, rightSide, g -> g.rightSide);
    }

    public S solve() {
        eliminateElementsBelowDiaginal();
        normalizeDiagonalElements();
        eliminateElementsAboveDiaginal();

        return solutionExtractor.apply(this);
    }

    // Gaussian elimination - create triangular matrix
    private void eliminateElementsBelowDiaginal() {
        for (int i = 0; i < equationCount - 1; i++) {
            // Find pivot in column 'i'
            int pivotRow = i;

            for (int row = i+1; row < equationCount; row++) {
                if (Math.abs (equationMatrix.getElement(row, i)) > Math.abs (equationMatrix.getElement(pivotRow, i)))
                    pivotRow = row;
            }

            // Swap pivot row and row 'i'
            Vectors.swap(equationMatrix.getRowVector(pivotRow), equationMatrix.getRowVector(i));
            Vectors.swap(rightSide.getRowVector(pivotRow), rightSide.getRowVector(i));

            // Elimination
            final double divider = equationMatrix.getElement(i, i);

            if (divider == 0)
                throw new RuntimeException("Matrix is singular");

            for (int row = i+1; row < equationCount; row++) {
                if (equationMatrix.getElement(row, i) != 0.0) {
                    final double coeff = -equationMatrix.getElement(row, i) / divider;

                    if (coeff != 0) {
                        equationMatrix.setRowVector (row, equationMatrix.getRowVector(row).multiplyAndAdd (equationMatrix.getRowVector(i), coeff));
                        rightSide.setRowVector (row, rightSide.getRowVector(row).multiplyAndAdd (rightSide.getRowVector(i), coeff));
                    }
                }
            }
        }
    }

    // Divide rows by diagonal elements.
    // Ensures that diagonal elements are 1.
    private void normalizeDiagonalElements() {
        for (int row = 0; row < equationCount; row++) {
            final double coeff = 1.0 / equationMatrix.getElement(row, row);

            for (int column = row; column < equationCount; column++)
                equationMatrix.setElement(row, column, equationMatrix.getElement(row, column) * coeff);

            for (int column = 0; column < rightSide.getColumnCount(); column++)
                rightSide.setElement(row, column, rightSide.getElement(row, column) * coeff);
        }
    }

    // Eliminate elements above diagonal.
    // For performance reasons the equation matrix is not eliminated, just right side is updated.
    private void eliminateElementsAboveDiaginal() {
        for (int i = equationCount - 1; i >= 0; i--) {
            for (int row = 0; row < i; row++) {
                final double coeff = -equationMatrix.getElement(row, i);

                if (coeff != 0) {
                    //orig.setRowVector (row, Vectors.multiplyAndAdd (orig.getRowVector(row), orig.getRowVector(i), coeff));
                    rightSide.setRowVector (row, rightSide.getRowVector(row).multiplyAndAdd (rightSide.getRowVector(i), coeff));
                }
            }
        }
    }

}
