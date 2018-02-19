package math;

import java.util.ArrayList;
import java.util.List;

public class GeneralLinearModel {
	private final int variableCount;
	private final int rightSideCount;
	
	private final IMatrix m;   // aT * a * w
	private final IMatrix b;   // Indices: rightSide, variable
	private IVectorRead ySquare;   // sum (w_i * y_i^2), index: rightSide

	public GeneralLinearModel (final Density density, final int variableCount, final int rightSideCount) {
		this.variableCount = variableCount;
		this.rightSideCount = rightSideCount;
		this.m = Matrices.createMutableMatrix(density, variableCount, variableCount);
		this.b = Matrices.createMutableMatrix(Density.DENSE, variableCount, rightSideCount);
		this.ySquare = Vectors.getZeroVector(rightSideCount);
	}
	
	public int getFeatureCount() {
		return variableCount;
	}

	// Adds equation - sparse version
	public void addSample (final IVectorRead coeffs, final IVectorRead rightSides, final double weight) {
		final double normalizedWeight = Math.sqrt(weight);
		
		for (IVectorIterator itA = coeffs.getNonZeroElementIterator(); itA.isValid(); itA.next()) {
			// m - elements below and above diagonal
			final int indexA = itA.getIndex();
			final double subProduct = itA.getElement() * normalizedWeight;

			for (IVectorIterator itB = coeffs.getNonZeroElementIterator(); itB.isValid() && itB.getIndex() < itA.getIndex(); itB.next()) {
				final double product = subProduct * itB.getElement();
				final double element = m.getElement(itA.getIndex(), itB.getIndex()) + product;
				
				m.setElement(itA.getIndex(), itB.getIndex(), element);
				m.setElement(itB.getIndex(), itA.getIndex(), element);
			}
			
			final double coeff = itA.getElement();
			final double product = coeff * coeff * normalizedWeight;
			m.setElement(indexA, indexA, m.getElement(indexA, indexA) + product);
			
			// B
			for (int rightSideIndex = 0; rightSideIndex < b.getColumnCount(); rightSideIndex++) {
				final double weightedRightSide = normalizedWeight * rightSides.getElement(rightSideIndex);
				
				final double rightSideProduct = weightedRightSide * itA.getElement();
				b.setElement(indexA, rightSideIndex, b.getElement(indexA, rightSideIndex) + rightSideProduct);
			}
		}
		
		
		
		ySquare = BinaryVectorAlgorithmOneNonzero.getInstance().processElements(
			ySquare,
			rightSides,
			(y, r) -> y + r*r*weight,
			new VectorSetter()
		).getVector();
	}

	public List<IVector> solveEquations() {
		checkEmptyColumns();
		
		// Gaussian elimination - create triangular matrix
		for (int i = 0; i < variableCount - 1; i++) {
			// Elimination. We do not find pivot element because it should be the diagonal. 
			final double divider = -m.getElement(i, i);
			
			if (Math.abs (divider) < 1e-8)
				throw new RuntimeException("Matrix is singular on column " + i);
			
			for (int row = i+1; row < variableCount; row++) {
				if (m.getElement(row, i) != 0) {
					final double coeff = m.getElement(row, i) / divider;
					
					m.setRowVector(row,
						BinaryVectorAlgorithmOneNonzero.getInstance().processElements(
							m.getRowVector(row),
							m.getRowVector(i),
							(a, b) -> a + b*coeff,
							new VectorSetter()
						).getVector()
					);
					
					b.setRowVector(row, 
						BinaryVectorAlgorithmOneNonzero.getInstance().processElements(
							b.getRowVector(row),
							b.getRowVector(i),
							(a, b) -> a + b*coeff,
							new VectorSetter()
						).getVector()
					);
				}
			}
		}
		
		// Back propagation
		final List<IVector> solution = new ArrayList<>(rightSideCount);
		
		for (int rightSide = 0; rightSide < rightSideCount; rightSide++) {
			final IVector singleSolution = Vectors.dense(variableCount);
			
			for (int row = variableCount - 1; row >= 0; row--) {
				double sum =
					b.getElement(row, rightSide) - 
					BinaryVectorAlgorithmBothNonzero.getInstance().processElements(
						m.getRowVector(row).subVector(row + 1, variableCount),
						singleSolution.subVector(row + 1, variableCount),
						(a, b) -> a * b,
						new VectorElementSum()
					).getSum();
				
				singleSolution.setElement(row, sum / m.getElement(row, row));
			}
			
			solution.add(singleSolution);
		}
		
		return solution;
	}

	private void checkEmptyColumns() {
		for (int column = 0; column < variableCount; column++) {
			boolean notNull = false;
			
			for (int row = 0; row < variableCount; row++)
				notNull |= m.getElement(row, column) != 0;
			
			if (!notNull)
				System.out.println("Null column " + column);
		}
	}

}
