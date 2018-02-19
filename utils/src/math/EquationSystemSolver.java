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
	
	// Adds equation - sparse version
	public void addEquation (final int[] coeffIndices, final double coeffs[], final double[] rightSides, final double weight) {
		final int coeffCount = coeffs.length;
		
		if (coeffIndices.length != coeffCount)
			throw new RuntimeException("Different lengths of array");
		
		// m - elements below and above diagonal
		final double normalizedWeight = Math.sqrt(weight);
		
		for (int i = 0; i < coeffCount; i++) {
			final int firstIndex = coeffIndices[i];
			final double subProduct = coeffs[i] * normalizedWeight;
			
			for (int j = 0; j < i; j++) {
				final int secondIndex = coeffIndices[j];
				final double product = subProduct * coeffs[j];
				final double element = m[firstIndex][secondIndex] + product;
				
				m[firstIndex][secondIndex] = element;
				m[secondIndex][firstIndex] = element;
			}
		}
		
		// m - diagonal
		for (int i = 0; i < coeffCount; i++) {
			final int index = coeffIndices[i];
			final double coeff = coeffs[i];
			final double product = coeff * coeff * normalizedWeight;
			m[index][index] += product;
		}
		
		// Updated B
		for (int rightSideIndex = 0; rightSideIndex < updatedB.length; rightSideIndex++) {
			final double weightedRightSide = normalizedWeight * rightSides[rightSideIndex];
			
			for (int i = 0; i < coeffCount; i++) {
				final int index = coeffIndices[i];

				final double product = weightedRightSide * coeffs[i];
				updatedB[rightSideIndex][index] += product;
			}
		}
	}
	
	// Adds equation - dense version
	public void addEquation (final double[] coeffs, final double[] rightSides, final double weight) {
		// Find non zero coeff indices
		int coeffCount = 0;
		
		for (int i = 0; i < variableCount; i++) {
			if (coeffs[i] != 0)
				coeffCount++;
		}
		
		final int[] nonZeroCoeffIndices = new int[coeffCount];
		final double[] nonZeroCoeffs = new double[coeffCount];
		
		int index = 0;
		
		for (int i = 0; i < variableCount; i++) {
			if (coeffs[i] != 0) {
				nonZeroCoeffIndices[index] = i;
				nonZeroCoeffs[index] = coeffs[i];
				index++;
			}
		}
	
		if (index != coeffCount)
			throw new RuntimeException("Internal error");
		
		addEquation(nonZeroCoeffIndices, nonZeroCoeffs, rightSides, weight);
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
