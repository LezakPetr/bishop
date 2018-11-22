package bishop.engine;

import java.io.PrintWriter;

import bishop.base.Position;

public class ConstantPositionEvaluator implements IPositionEvaluator {
	
	private final IPositionEvaluation tacticalEvaluation;
	private final IPositionEvaluation positionalEvaluation;
	
	public ConstantPositionEvaluator (final IPositionEvaluation tacticalEvaluation, final IPositionEvaluation positionalEvaluation) {
		this.tacticalEvaluation = tacticalEvaluation;
		this.positionalEvaluation = positionalEvaluation;
	}
	
	@Override
	public IPositionEvaluation evaluateTactical(final Position position, final AttackCalculator attackCalculator, final MobilityCalculator mobilityCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTableGroup.ZERO_GROUP, mobilityCalculator);
		
		return tacticalEvaluation;		
	}

	@Override
	public IPositionEvaluation evaluatePositional (final AttackCalculator attackCalculator) {
		return positionalEvaluation;
	}

	/**
	 * Writes information about the position into given writer.
	 * @param writer target writer
	 */
	public void writeLog (final PrintWriter writer) {
	}

}
