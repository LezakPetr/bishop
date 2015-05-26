package bishop.engine;

public class MaterialPositionEvaluatorFactory implements IPositionEvaluatorFactory {

	@Override
	public IPositionEvaluator createEvaluator() {
		return new MaterialPositionEvaluator();
	}

}
