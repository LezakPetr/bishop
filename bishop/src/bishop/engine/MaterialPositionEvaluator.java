package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.DefaultAdditiveMaterialEvaluator;
import bishop.base.Position;

public final class MaterialPositionEvaluator implements IPositionEvaluator {
	
	private final IPositionEvaluation evaluation;
	
	public MaterialPositionEvaluator(final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluation = evaluationFactory.get();
	}
	
	public IPositionEvaluation evaluatePosition (final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		evaluation.clear();
		evaluation.addEvaluation(DefaultAdditiveMaterialEvaluator.getInstance().evaluateMaterial(position));
		
		return evaluation;
	}
	
	public void writeLog (final PrintWriter writer) {
	}

}
