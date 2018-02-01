package neural;

public interface IPerceptronLayer {

	public int getInputNodeCount();
	
	public void initialize();
	public void addInput (final int index, final float value);
	public void addPositiveUnityInput (final int index);
	public void addNegativeUnityInput (final int index);
	public void propagate();
}
