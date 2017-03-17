package bishop.engine;


import java.util.function.Supplier;


public final class MaterialPositionEvaluator extends ConstantPositionEvaluator {
	
	public MaterialPositionEvaluator(final Supplier<IPositionEvaluation> evaluationFactory) {
		super (evaluationFactory.get(), evaluationFactory.get());   // Zero constant tactical and positional evaluation 
	}
	
}
