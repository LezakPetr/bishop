package bishop.engine;

import java.io.PrintWriter;

import parallel.Parallel;
import bishop.base.DefaultAdditiveMaterialEvaluator;
import bishop.base.IMaterialEvaluator;
import bishop.base.Position;

public class DrawPositionEvaluator implements IPositionEvaluator {

	private static final int EVALUATION_SHIFT = 3;

	private final IMaterialEvaluator materialEvaluator;
	private int evaluation;
	
	
	public DrawPositionEvaluator(final IMaterialEvaluator materialEvaluator) {
		this.materialEvaluator = materialEvaluator;
	}

	@Override
	public int evaluatePosition(final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		evaluation = materialEvaluator.evaluateMaterial(position) >> EVALUATION_SHIFT;
		
		return evaluation;
	}
	
	public void writeLog (final PrintWriter writer) {
		writer.println ("Draw evaluation: " + Evaluation.toString (evaluation));
	}

}
