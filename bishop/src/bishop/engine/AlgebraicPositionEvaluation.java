package bishop.engine;

import java.util.function.Supplier;

import bishop.base.Color;

public class AlgebraicPositionEvaluation implements IPositionEvaluation {

	private static final Supplier<IPositionEvaluation> TESTING_FACTORY = () -> new AlgebraicPositionEvaluation(new PositionEvaluationCoeffs());
	
	private final PositionEvaluationCoeffs coeffs;
	private int evaluation;
	
	public AlgebraicPositionEvaluation (final PositionEvaluationCoeffs coeffs) {
		this.coeffs = coeffs;
	}
	
	@Override
	public int getEvaluation() {
		return evaluation;
	}

	@Override
	public void clear(final int onTurn) {
		evaluation = 0;
	}
	
	@Override
	public void addCoeffWithCount(final int index, final int count) {
		this.evaluation += count * coeffs.getEvaluationCoeff(index);
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
