package bishop.engine;

import java.util.function.Supplier;

import bishop.base.Color;

public class AlgebraicPositionEvaluation implements IPositionEvaluation {

	private static final Supplier<IPositionEvaluation> TESTING_FACTORY = () -> new AlgebraicPositionEvaluation(new PositionEvaluationCoeffs());
	
	private PositionEvaluationCoeffs coeffs;
	private int evaluation;
	
	public AlgebraicPositionEvaluation (final PositionEvaluationCoeffs coeffs) {
		this.coeffs = coeffs;
	}
	
	@Override
	public int getEvaluation() {
		return evaluation;
	}

	@Override
	public void clear() {
		evaluation = 0;
	}
	
	@Override
	public void addEvaluation (final int evaluation) {
		this.evaluation += evaluation;
	}

	@Override
	public void addCoeffWithCount(final int index, final int count) {
		this.evaluation += count * coeffs.getEvaluationCoeff(index);
	}
	
	@Override
	public void shiftRight (final int shift) {
		this.evaluation >>= shift;
	}
	
	@Override
	public void addSubEvaluation (final IPositionEvaluation subEvaluation) {
		this.evaluation += subEvaluation.getEvaluation();
	}
	
	@Override
	public void addCoeff(final int index, final int color, final int count) {
		final int signedCount = (color == Color.WHITE) ? +count : -count;
		
		addCoeffWithCount(index, signedCount);
	}

	@Override
	public void addCoeff(final int index, final int color) {
		addCoeff(index, color, 1);
	}

	public static Supplier<IPositionEvaluation> getTestingFactory() {
		return TESTING_FACTORY;
	}

}