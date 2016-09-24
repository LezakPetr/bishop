package bishop.engine;

import java.io.PrintWriter;

import parallel.Parallel;
import bishop.base.AdditiveMaterialEvaluator;
import bishop.base.Position;

public class DrawPositionEvaluator implements IPositionEvaluator {

	private static final int EVALUATION_SHIFT = 3;
	
	private int evaluation;
	
	
	public DrawPositionEvaluator() {
	}

	@Override
	public int evaluatePosition(final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		evaluation = AdditiveMaterialEvaluator.getInstance().evaluateMaterial(position) >> EVALUATION_SHIFT;
		
		return evaluation;
	}
	
	public void writeLog (final PrintWriter writer) {
		writer.println ("Draw evaluation: " + Evaluation.toString (evaluation));
	}

}
