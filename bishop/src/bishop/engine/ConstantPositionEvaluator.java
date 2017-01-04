package bishop.engine;

import java.io.PrintWriter;

import bishop.base.Position;

public class ConstantPositionEvaluator implements IPositionEvaluator {
	
	private IPositionEvaluation evaluation;
	
	public ConstantPositionEvaluator (final IPositionEvaluation evaluation) {
		this.evaluation = evaluation;
	}
	
	@Override
	public IPositionEvaluation evaluatePosition (final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		return evaluation;
	}
	
	/**
	 * Writes information about the position into given writer.
	 * @param writer target writer
	 */
	public void writeLog (final PrintWriter writer) {
		writer.println ("Evaluation: " + evaluation.toString());
	}

}
