package neural;

public class IdentityActivationFunction implements IActivationFunction {

	private static final IdentityActivationFunction INSTANCE = new IdentityActivationFunction();
	
	private IdentityActivationFunction() {
	}
	
	@Override
	public float apply(final float stimulus) {
		return stimulus;
	}

	@Override
	public float derivate(final float stimulus) {
		return 1.0f;
	}

	public static IdentityActivationFunction getInstance() {
		return INSTANCE;
	}
}
