package bishop.engine;

import java.util.function.Supplier;
import bishop.base.IMaterialEvaluator;

public class PositionEvaluatorSwitchFactory implements IPositionEvaluatorFactory {
	
	private final IMaterialEvaluator materialEvaluator;
	private final PositionEvaluatorSwitchSettings settings;
	private final Supplier<IPositionEvaluation> evaluationFactory;

	public PositionEvaluatorSwitchFactory (final PositionEvaluatorSwitchSettings settings, final IMaterialEvaluator materialEvaluator, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.materialEvaluator = materialEvaluator;
		this.settings = settings;
		this.evaluationFactory = evaluationFactory;
	}
	
	@Override
	public IPositionEvaluator createEvaluator() {
		return new PositionEvaluatorSwitch(settings, materialEvaluator, evaluationFactory);
	}

}
