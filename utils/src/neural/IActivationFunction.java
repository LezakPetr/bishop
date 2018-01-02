package neural;

public interface IActivationFunction {
	public float apply (final float stimulus);
	public float derivate (final float stimulus);
}
