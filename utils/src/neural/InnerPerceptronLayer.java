package neural;


public class InnerPerceptronLayer<N extends IPerceptronLayer> implements IPerceptronLayer {

	protected final IActivationFunction activationFunction;
	protected final float[] stimuli;
	protected final float[][] weights;
	protected final float[] biases;
	protected final N nextLayer;
	
	public InnerPerceptronLayer (final IActivationFunction activationFunction, final int inputNodeCount, final N nextLayer) {
		final int outputNodeCount = nextLayer.getInputNodeCount();
		
		this.activationFunction = activationFunction;
		this.stimuli = new float[outputNodeCount];
		this.weights = new float[inputNodeCount][outputNodeCount];
		this.biases = new float[outputNodeCount];
		this.nextLayer = nextLayer;
	}
	
	
	@Override
	public int getInputNodeCount() {
		return weights.length;
	}

	@Override
	public int getOutputNodeCount() {
		return stimuli.length;
	}

	@Override
	public void initialize() {
		System.arraycopy(biases, 0, stimuli, 0, biases.length);
	}

	@Override
	public void addInput(final int index, final float value) {
		final float[] weightsRow = weights[index];
		
		for (int i = 0; i < stimuli.length; i++)
			stimuli[i] += value * weightsRow[i];
	}
	
	@Override
	public void propagate() {
		for (int i = 0; i < stimuli.length; i++)
			nextLayer.addInput(i, activationFunction.apply(stimuli[i]));
	}

}
