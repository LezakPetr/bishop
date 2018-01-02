package neural;

public interface IPerceptronLayer {

	public int getInputNodeCount();
	public int getOutputNodeCount();
	
	public void initialize();
	public void addInput (final int index, final float value);
	public void propagate();
}
