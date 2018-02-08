package bishop.engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import utils.IoUtils;


public class CoeffCountPositionEvaluation extends AlgebraicPositionEvaluation {
	
	private final boolean[] coeffFilled = new boolean[PositionEvaluationCoeffs.LAST];
	private final int[] coeffCounts = new int[PositionEvaluationCoeffs.LAST];
	private final int[] nonZeroCoeffs = new int[PositionEvaluationCoeffs.LAST];
	private int nonZeroCoeffCount;
	
	public CoeffCountPositionEvaluation(final PositionEvaluationCoeffs coeffs) {
		super (coeffs);
	}

	@Override
	public void clear(final int onTurn) {
		super.clear(onTurn);
		
		Arrays.fill(coeffCounts, 0);
		Arrays.fill(coeffFilled, false);
		nonZeroCoeffCount = 0;
	}
	
	@Override
	public void addCoeffWithCount(final int index, final int count) {
		if (count != 0) {
			super.addCoeffWithCount(index, count);
			
			coeffCounts[index] += count;
			
			if (!coeffFilled[index]) {
				coeffFilled[index] = true;
				nonZeroCoeffs[nonZeroCoeffCount] = index;
				nonZeroCoeffCount++;
			}
		}
	}
		
	public double getCoeffCount (final int index) {
		return coeffCounts[index];
	}
	
	@Override
	public String toString() {
		final CoeffRegistry registry = PositionEvaluationCoeffs.getCoeffRegistry();
		final StringBuilder result = new StringBuilder();
		
		for (int coeff = 0; coeff < PositionEvaluationCoeffs.LAST; coeff++) {
			final double count = getCoeffCount(coeff);
			
			if (count != 0) {
				result.append (registry.getName(coeff));
				result.append (" = ");
				result.append (count);
				result.append (IoUtils.NEW_LINE);
			}
		}
		
		return result.toString();
	}
	
	public int genNonZeroCoeffCount() {
		return nonZeroCoeffCount;
	}
	
	public int getNonZeroCoeffAt (final int nonZeroCoeffIndex) {
		return nonZeroCoeffs[nonZeroCoeffIndex];
	}
	
}
