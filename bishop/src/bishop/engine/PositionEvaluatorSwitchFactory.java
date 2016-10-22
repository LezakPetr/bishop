package bishop.engine;

import java.io.IOException;

import bishop.base.DefaultAdditiveMaterialEvaluator;
import bishop.base.IMaterialEvaluator;

public class PositionEvaluatorSwitchFactory implements IPositionEvaluatorFactory {
	
	private final IMaterialEvaluator materialEvaluator;
	private final PositionEvaluatorSwitchSettings settings;

	public PositionEvaluatorSwitchFactory (final PositionEvaluatorSwitchSettings settings, final IMaterialEvaluator materialEvaluator) {
		this.materialEvaluator = materialEvaluator;
		this.settings = settings;
	}
	
	@Override
	public IPositionEvaluator createEvaluator() {
		return new PositionEvaluatorSwitch(settings, materialEvaluator);
	}

}
