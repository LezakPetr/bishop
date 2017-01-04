package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.IMaterialEvaluator;
import bishop.base.Position;

public class DrawPositionEvaluator implements IPositionEvaluator {

	private static final int EVALUATION_SHIFT = 3;

	private final IMaterialEvaluator materialEvaluator;
	private final IPositionEvaluation evaluation;
	
	
	public DrawPositionEvaluator(final IMaterialEvaluator materialEvaluator, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.materialEvaluator = materialEvaluator;
		this.evaluation = evaluationFactory.get();
	}

	@Override
	public IPositionEvaluation evaluatePosition(final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		evaluation.clear();
		evaluation.addEvaluation(materialEvaluator.evaluateMaterial(position));
		evaluation.shiftRight(EVALUATION_SHIFT);
		
		return evaluation;
	}
	
	public void writeLog (final PrintWriter writer) {
		writer.println ("Draw evaluation: " + evaluation.toString());
	}

}
