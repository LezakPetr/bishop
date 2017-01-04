package bishop.engine;

import java.util.function.Supplier;

public class MaterialPositionEvaluatorFactory implements IPositionEvaluatorFactory {

	private final Supplier<IPositionEvaluation> evaluationFactory;
	
	public MaterialPositionEvaluatorFactory(final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluationFactory = evaluationFactory;
	}
	
	@Override
	public IPositionEvaluator createEvaluator() {
		return new MaterialPositionEvaluator(evaluationFactory);
	}

}
