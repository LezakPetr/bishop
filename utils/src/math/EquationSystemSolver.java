package math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EquationSystemSolver {
	
	private final int variableCount;
	
	private final double[][] m;   // aT * a * w
	private final double[][] updatedB;   // Indices: rightSide, variable
	
	
	public EquationSystemSolver (final int variableCount, final int rightSideCount) {
		this.variableCount = variableCount;
		this.m = new double[variableCount][variableCount];
		this.updatedB = new double[rightSideCount][variableCount];
	}
	
	public void addEquation (final double[] coeffs, final double[] rightSides, final double weight) {
		// Find non zero coeff indices
		final int[] nonZeroCoeffIndices = new int[variableCount];
		int coeffCount = 0;
		
		for (int i = 0; i < variableCount; i++) {
			if (coeffs[i] != 0) {
				nonZeroCoeffIndices[coeffCount] = i;
				coeffCount++;
			}
		}
		
		// m - elements below and above diagonal
		final double normalizedWeight = Math.sqrt(weight);
		
		for (int i = 0; i < coeffCount; i++) {
			final int firstIndex = nonZeroCoeffIndices[i];
			final double subProduct = coeffs[firstIndex] * normalizedWeight;
			
			for (int j = 0; j < i; j++) {
				final int secondIndex = nonZeroCoeffIndices[j];
				final double product = subProduct * coeffs[secondIndex];
				final double element = m[firstIndex][secondIndex] + product;
				
				m[firstIndex][secondIndex] = element;
				m[secondIndex][firstIndex] = element;
			}
		}
		
		// m - diagonal
		for (int i = 0; i < coeffCount; i++) {
			final int index = nonZeroCoeffIndices[i];
			final double coeff = coeffs[index];
			final double product = coeff * coeff * normalizedWeight;
			m[index][index] += product;
		}
		
		// Updated B
		for (int rightSideIndex = 0; rightSideIndex < updatedB.length; rightSideIndex++) {
			final double weightedRightSide = normalizedWeight * rightSides[rightSideIndex];
			
			for (int i = 0; i < coeffCount; i++) {
				final int index = nonZeroCoeffIndices[i];

				final double product = weightedRightSide * coeffs[index];
				updatedB[rightSideIndex][index] += product;
			}
		}
	}
	
	public List<List<Double>> solveEquations() {
		checkEmptyColumns();
		
		// Gaussian elimination - create triangular matrix
		for (int i = 0; i < variableCount - 1; i++) {
			// Elimination. We do not find pivot element because it should be the diagonal. 
			final double divider = -m[i][i];
			
			if (Math.abs (divider) < 1e-8)
				throw new RuntimeException("Matrix is singular on column " + i);
			
			for (int row = i+1; row < variableCount; row++) {
				if (m[row][i] != 0) {
					final double coeff = m[row][i] / divider;
					
					for (int k = 0; k < variableCount; k++) {
						m[row][k] += m[i][k] * coeff;
					}
					
					for (int rightSide = 0; rightSide < updatedB.length; rightSide++)
						updatedB[rightSide][row] += updatedB[rightSide][i] * coeff;
				}
			}
		}
		
		// Back propagation
		final List<List<Double>> solution = new ArrayList<>(updatedB.length);
		
		for (int rightSide = 0; rightSide < updatedB.length; rightSide++) {
			final List<Double> singleSolution = new ArrayList<>(variableCount);
			singleSolution.addAll(Collections.nCopies(variableCount, null));
			
			for (int row = variableCount - 1; row >= 0; row--) {
				double sum = updatedB[rightSide][row];
				
				for (int i = row + 1; i < variableCount; i++)
					sum -= m[row][i] * singleSolution.get(i);
				
				singleSolution.set (row, sum / m[row][row]);
			}
			
			solution.add(singleSolution);
		}
		
		return solution;
	}

	private void checkEmptyColumns() {
		for (int column = 0; column < variableCount; column++) {
			boolean notNull = false;
			
			for (int row = 0; row < variableCount; row++)
				notNull |= m[row][column] != 0;
			
			if (!notNull)
				System.out.println("Null column " + column);
		}
	}

	public int getVariableCount() {
		return variableCount;
	}

	public int getRightSideCount() {
		return updatedB.length;
	}

}
