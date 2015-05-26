package bishop.engine;

import java.io.PrintWriter;
import bishop.base.Position;

public final class MaterialPositionEvaluator implements IPositionEvaluator {
	
	public int evaluatePosition (final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.ZERO_TABLE);
		
		return position.getMaterialEvaluation();
	}
	
	public void writeLog (final PrintWriter writer) {
	}

}
