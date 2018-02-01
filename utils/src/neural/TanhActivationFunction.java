package neural;

public class TanhActivationFunction implements IActivationFunction {
	
	private static final TanhActivationFunction INSTANCE = new TanhActivationFunction();
	
	private TanhActivationFunction() {
	}
	
	@Override
	public float apply (final float stimulus) {
		return (float) Math.tanh(stimulus);
	}
	
	@Override
	public float derivate (final float stimulus) {
		final double pow = Math.exp(-2 * stimulus);
		
		return (float) (4.0 /(1.0/pow + 2 + pow));	
	}
	
	public static TanhActivationFunction getInstance() {
		return INSTANCE;
	}
}
