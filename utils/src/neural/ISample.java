package neural;

public interface ISample {
	public int getInputIndex(final int nonZeroIndex);

	public float getInputValue(final int nonZeroIndex);
	
	public int getNonZeroInputCount();

	public float getOutput(final int index);

	public float getWeight();
}
