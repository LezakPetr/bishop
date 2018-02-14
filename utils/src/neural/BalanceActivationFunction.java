package neural;

public class BalanceActivationFunction implements IActivationFunction {
	
	private static final BalanceActivationFunction INSTANCE = new BalanceActivationFunction();
	
	private BalanceActivationFunction() {
	}
	
	@Override
	public float apply (final float stimulus) { 
		final double pow = Math.exp(-2.0 * stimulus);
		
		return (float) (2.0 / (1 + pow) - 1);
	}
	
	@Override
	public float derivate (final float stimulus) {
		final double pow = Math.exp(-2 * stimulus);
		
		return (float) (4.0 /(1.0/pow + 2 + pow));	
	}
	
	public static BalanceActivationFunction getInstance() {
		return INSTANCE;
	}
}
