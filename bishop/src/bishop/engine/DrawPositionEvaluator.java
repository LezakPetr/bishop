package bishop.engine;

import java.util.function.Supplier;

public class DrawPositionEvaluator extends ConstantPositionEvaluator {

	private static final int EVALUATION_SHIFT = 3;
	
	
	public DrawPositionEvaluator(final Supplier<IPositionEvaluation> evaluationFactory) {
		super (evaluationFactory.get(), evaluationFactory.get());
	}
	
	@Override
	public int getMaterialEvaluationShift() {
		return EVALUATION_SHIFT;
	}

}
