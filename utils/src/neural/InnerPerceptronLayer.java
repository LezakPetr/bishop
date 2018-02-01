package neural;

import java.util.Arrays;

public class InnerPerceptronLayer<N extends IPerceptronLayer> implements IPerceptronLayer {

	protected final float[] stimuli;
	protected final PerceptronLayerSettings settings;
	protected final N nextLayer;
	
	public InnerPerceptronLayer (final PerceptronLayerSettings settings, final N nextLayer) {
		final int outputNodeCount = nextLayer.getInputNodeCount();
		
		this.stimuli = new float[outputNodeCount];
		this.settings = settings;
		this.nextLayer = nextLayer;
	}
	
	
	@Override
	public int getInputNodeCount() {
		return settings.weights.length;
	}

	protected int getOutputNodeCount() {
		return stimuli.length;
	}

	@Override
	public void initialize() {
		System.arraycopy(settings.biases, 0, stimuli, 0, settings.biases.length);
	}

	@Override
	public void addInput(final int index, final float value) {
		final float[] weightsRow = settings.weights[index];
		
		for (int i = 0; i < stimuli.length; i++)
			stimuli[i] += value * weightsRow[i];
	}

	@Override
	public void addPositiveUnityInput(final int index) {
		final float[] weightsRow = settings.weights[index];
		
		for (int i = 0; i < stimuli.length; i++)
			stimuli[i] += weightsRow[i];
	}

	@Override
	public void addNegativeUnityInput(final int index) {
		final float[] weightsRow = settings.weights[index];
		
		for (int i = 0; i < stimuli.length; i++)
			stimuli[i] -= weightsRow[i];
	}
	
	@Override
	public void propagate() {
		for (int i = 0; i < stimuli.length; i++)
			nextLayer.addInput(i, settings.activationFunction.apply(stimuli[i]));
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		
		result.append("Stimuli = " + Arrays.toString(stimuli));
		result.append(", biases = " + Arrays.toString(settings.biases));
		result.append(", weights = " + Arrays.toString(settings.weights));
		
		return result.toString();
	}

}
