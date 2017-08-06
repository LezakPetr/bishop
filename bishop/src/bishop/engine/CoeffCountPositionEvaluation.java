package bishop.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import utils.IoUtils;


public class CoeffCountPositionEvaluation extends AlgebraicPositionEvaluation {
	
	private static final int COEFF_COUNT_SHIFT = 10;
	private static final int COEFF_MULTIPLICATOR = 1 << COEFF_COUNT_SHIFT;
	
	private int constantEvaluation;
	private final Map<Integer, Integer> coeffCounts;
	
	public CoeffCountPositionEvaluation(final PositionEvaluationCoeffs coeffs) {
		super (coeffs);
		
		this.coeffCounts = new HashMap<>();
	}

	@Override
	public void clear() {
		super.clear();
		
		constantEvaluation = 0;
		coeffCounts.clear();
	}
	
	@Override
	public void addEvaluation (final int evaluation) {
		super.addEvaluation(evaluation);
		
		this.constantEvaluation += evaluation;
	}

	@Override
	public void addCoeffWithCount(final int index, final int count) {
		super.addCoeffWithCount(index, count);
		
		final int shiftedCount = count << COEFF_COUNT_SHIFT;
		
		coeffCounts.merge(index, shiftedCount, Integer::sum);
	}
	
	@Override
	public void shiftRight (final int shift) {
		super.shiftRight(shift);
		
		constantEvaluation >>= shift;
		
		coeffCounts.replaceAll((k, v) -> k >> shift);
	}
	
	@Override
	public void addSubEvaluation (final IPositionEvaluation subEvaluation) {
		super.addSubEvaluation(subEvaluation);
		
		if (subEvaluation instanceof CoeffCountPositionEvaluation) {
			final CoeffCountPositionEvaluation subCoeffEvaluation = (CoeffCountPositionEvaluation) subEvaluation;
			
			this.constantEvaluation += subCoeffEvaluation.constantEvaluation;
			
			subCoeffEvaluation.coeffCounts.forEach(
				(k, v) -> this.coeffCounts.merge(k, v, Integer::sum)	
			);
		}
		else
			this.constantEvaluation += subEvaluation.getEvaluation();
	}
	
	public int getConstantEvaluation() {
		return constantEvaluation;
	}
	
	public double getCoeffCount (final int index) {
		return (double) coeffCounts.getOrDefault(index, 0) / (double) COEFF_MULTIPLICATOR;
	}
	
	@Override
	public String toString() {
		final CoeffRegistry registry = PositionEvaluationCoeffs.getCoeffRegistry();
		final StringBuilder result = new StringBuilder();
		
		for (Integer coeff: coeffCounts.keySet()) {
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
	
	public Set<Integer> getNonZeroCoeffs() {
		return Collections.unmodifiableSet(coeffCounts.keySet());
	}

}
