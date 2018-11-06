package bishop.engine;

import java.util.*;

import utils.IoUtils;


public class CoeffCountPositionEvaluation extends AlgebraicPositionEvaluation {
	
	private static final int COEFF_COUNT_SHIFT = 8;
	public static final int COEFF_MULTIPLICATOR = 1 << COEFF_COUNT_SHIFT;
	
	private int constantEvaluation;
	private final Map<Integer, Integer> coeffCounts;
	
	public CoeffCountPositionEvaluation(final PositionEvaluationCoeffs coeffs) {
		super (coeffs);
		
		this.coeffCounts = new TreeMap<>();
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
		
		coeffCounts.replaceAll((k, v) -> v >> shift);
	}

	public void addSubEvaluation (final IPositionEvaluation subEvaluation) {
		addSubEvaluation(subEvaluation, 1);
	}
	
	@Override
	public void addSubEvaluation (final IPositionEvaluation subEvaluation, final int coeff) {
		super.addSubEvaluation(subEvaluation, coeff);
		
		if (subEvaluation instanceof CoeffCountPositionEvaluation) {
			final CoeffCountPositionEvaluation subCoeffEvaluation = (CoeffCountPositionEvaluation) subEvaluation;
			
			subCoeffEvaluation.coeffCounts.forEach(
				(k, v) -> this.coeffCounts.merge(k, coeff * v, Integer::sum)
			);
		}

		this.constantEvaluation += coeff * subEvaluation.getEvaluation();
	}
	
	public int getConstantEvaluation() {
		return constantEvaluation;
	}
	
	public double getCoeffCount (final int index) {
		return (double) getCoeffCountRaw(index) / (double) COEFF_MULTIPLICATOR;
	}

	public int getCoeffCountRaw (final int index) {
		return coeffCounts.getOrDefault(index, 0);
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
