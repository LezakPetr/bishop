package math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class EquationSystemSolver {
	
	private final GeneralLinearModel model;
	
	public EquationSystemSolver (final int variableCount, final int rightSideCount) {
		model = new GeneralLinearModel(Density.DENSE, variableCount, rightSideCount);
	}
	
	public void addEquation (final IVectorRead coeffs, final IVectorRead rightSides, final double weight) {
		model.addSample (coeffs, rightSides, weight);
	}
	
	// Adds equation - dense version
	public void addEquation (final double[] coeffs, final double[] rightSides, final double weight) {
		// Find non zero coeff indices
		int coeffCount = 0;
		
		for (int i = 0; i < model.getFeatureCount(); i++) {
			if (coeffs[i] != 0)
				coeffCount++;
		}
		
		final int[] nonZeroCoeffIndices = new int[coeffCount];
		final double[] nonZeroCoeffs = new double[coeffCount];
		
		int index = 0;
		
		for (int i = 0; i < model.getFeatureCount(); i++) {
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
	

	public int getVariableCount() {
		return variableCount;
	}

	public int getRightSideCount() {
		return updatedB.length;
	}

}
