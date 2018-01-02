package neural;

public class BalanceActivationFunction implements IActivationFunction {
	@Override
	public float apply (final float stimulus) {
		final double pow = Math.exp(-2.0 * stimulus);
		
		return (float) ((1 - pow) / (1 + pow));
	}
	
	@Override
	public float derivate (final float stimulus) {
		final double pow = Math.exp(-2 * stimulus);
		final double d = 1 + pow;
		
		return (float) (4.0 * pow / (d * d));
				
	}
}
