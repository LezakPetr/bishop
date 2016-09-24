package bishop.engine;

import java.io.PrintWriter;

import parallel.Parallel;
import bishop.base.AdditiveMaterialEvaluator;
import bishop.base.Position;

public final class MaterialPositionEvaluator implements IPositionEvaluator {
	
	public int evaluatePosition (final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		return AdditiveMaterialEvaluator.getInstance().evaluateMaterial(position);
	}
	
	public void writeLog (final PrintWriter writer) {
	}

}
