package bishop.engine;

import java.io.PrintWriter;

import parallel.Parallel;

import bishop.base.Position;

public class ConstantPositionEvaluator implements IPositionEvaluator {
	
	private int evaluation;
	
	public ConstantPositionEvaluator (final int evaluation) {
		this.evaluation = evaluation;
	}
	
	@Override
	public int evaluatePosition (final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		return evaluation;
	}
	
	/**
	 * Writes information about the position into given writer.
	 * @param writer target writer
	 */
	public void writeLog (final PrintWriter writer) {
		writer.println ("Evaluation: " + Evaluation.toString (evaluation));
	}

}
