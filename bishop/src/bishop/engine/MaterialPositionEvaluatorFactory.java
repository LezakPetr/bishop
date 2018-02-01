package bishop.engine;


public class MaterialPositionEvaluatorFactory implements IPositionEvaluatorFactory {
		
	@Override
	public IPositionEvaluator createEvaluator(final IPositionEvaluation evaluation) {
		return new MaterialPositionEvaluator(evaluation);
	}

}
