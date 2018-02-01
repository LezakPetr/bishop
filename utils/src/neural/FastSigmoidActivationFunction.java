package neural;

public class FastSigmoidActivationFunction implements IActivationFunction {
	
	private static final FastSigmoidActivationFunction INSTANCE = new FastSigmoidActivationFunction();
	
	private FastSigmoidActivationFunction() {
	}
	
	@Override
	public float apply (final float stimulus) {
		return stimulus / (1.0f + Math.abs(stimulus));
	}
	
	@Override
	public float derivate (final float stimulus) {
		final float d = 1.0f + Math.abs(stimulus);
		
		return 1.0f / (d*d);	
	}
	
	public static FastSigmoidActivationFunction getInstance() {
		return INSTANCE;
	}
}