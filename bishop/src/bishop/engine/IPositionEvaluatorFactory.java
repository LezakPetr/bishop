package bishop.engine;

public interface IPositionEvaluatorFactory {
	public IPositionEvaluator createEvaluator(final IPositionEvaluation evaluation);
}
