package neural;

public interface ILearningInnerPerceptronLayer extends ILearningPerceptronLayer {
	public int getOutputNodeCount();

	public void updateBias(final int outputIndex, final float step);

	public void updateWeight(final int inputIndex, final int outputIndex, final float step);

	public void setWeight(final int inputIndex, final int outputIndex, final float value);
	public void setBias(final int outputIndex, final float value);
	public PerceptronLayerSettings getSettings();
}
