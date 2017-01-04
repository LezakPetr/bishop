package bishop.engine;

import java.util.Arrays;


public class CoeffCountPositionEvaluation extends AlgebraicPositionEvaluation {
	
	private static final int COEFF_COUNT_SHIFT = 10;
	private static final int COEFF_MULTIPLICATOR = 1 << COEFF_COUNT_SHIFT;
	
	private int constantEvaluation;
	private final int[] coeffCounts;
	
	public CoeffCountPositionEvaluation(final PositionEvaluationCoeffs coeffs) {
		super (coeffs);
		
		this.coeffCounts = new int[PositionEvaluationCoeffs.LAST];
	}

	@Override
	public void clear() {
		super.clear();
		
		constantEvaluation = 0;
		Arrays.fill(coeffCounts, 0);
	}
	
	@Override
	public void addEvaluation (final int evaluation) {
		super.addEvaluation(evaluation);
		
		this.constantEvaluation += evaluation;
	}

	@Override
	public void addCoeffWithCount(final int index, final int count) {
		super.addCoeffWithCount(index, count);
		
		coeffCounts[index] += count << COEFF_COUNT_SHIFT;
	}
	
	@Override
	public void shiftRight (final int shift) {
		super.shiftRight(shift);
		
		constantEvaluation >>= shift;
		
		for (int i = PositionEvaluationCoeffs.FIRST; i < PositionEvaluationCoeffs.LAST; i++)
			coeffCounts[i] >>= shift;
	}
	
	@Override
	public void addSubEvaluation (final IPositionEvaluation subEvaluation) {
		super.addSubEvaluation(subEvaluation);
		
		if (subEvaluation instanceof CoeffCountPositionEvaluation) {
			final CoeffCountPositionEvaluation subCoeffEvaluation = (CoeffCountPositionEvaluation) subEvaluation;
			
			this.constantEvaluation += subCoeffEvaluation.constantEvaluation;
			
			for (int i = PositionEvaluationCoeffs.FIRST; i < PositionEvaluationCoeffs.LAST; i++)
				this.coeffCounts[i] += subCoeffEvaluation.coeffCounts[i];
		}
		else
			this.constantEvaluation += subEvaluation.getEvaluation();
	}
	
	public int getConstantEvaluation() {
		return constantEvaluation;
	}
	
	public double getCoeffCount (final int index) {
		return (double) coeffCounts[index] / (double) COEFF_MULTIPLICATOR;
	}

}
