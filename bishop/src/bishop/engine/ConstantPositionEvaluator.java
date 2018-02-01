package bishop.engine;

import java.io.PrintWriter;

import bishop.base.Position;

public class ConstantPositionEvaluator implements IPositionEvaluator {
	
	private final int constantEvaluation;
	private final IPositionEvaluation evaluation;
	
	public ConstantPositionEvaluator (final IPositionEvaluation evaluation, final int constantEvaluation) {
		this.evaluation = evaluation;
		this.constantEvaluation = constantEvaluation;
	}
	
	@Override
	public void evaluate (final Position position, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTableGroup.ZERO_GROUP);
		
		this.evaluation.addCoeffWithCount(PositionEvaluationCoeffs.EVALUATION_COEFF, constantEvaluation);
	}

	/**
	 * Writes information about the position into given writer.
	 * @param writer target writer
	 */
	public void writeLog (final PrintWriter writer) {
	}

	@Override
	public IPositionEvaluation getEvaluation() {
		return evaluation;
	}

}
