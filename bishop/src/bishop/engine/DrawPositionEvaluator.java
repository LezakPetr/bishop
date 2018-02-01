package bishop.engine;


public class DrawPositionEvaluator extends ConstantPositionEvaluator {

	private static final int EVALUATION_SHIFT = 3;
	
	
	public DrawPositionEvaluator(final IPositionEvaluation evaluation) {
		super (evaluation, Evaluation.DRAW);
	}
	
	@Override
	public int getMaterialEvaluationShift() {
		return EVALUATION_SHIFT;
	}

}
