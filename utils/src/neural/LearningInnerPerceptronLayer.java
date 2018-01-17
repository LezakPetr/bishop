package neural;

import java.util.Arrays;

public class LearningInnerPerceptronLayer extends InnerPerceptronLayer<ILearningPerceptronLayer> implements ILearningInnerPerceptronLayer {

	private final float[] inputs;
	private final float[] stimuliErrors;
			
	public LearningInnerPerceptronLayer (final IActivationFunction activationFunction, final int inputNodeCount, final ILearningPerceptronLayer nextLayer) {
		super (activationFunction, inputNodeCount, nextLayer);
		
		inputs = new float[inputNodeCount];
		
		final int outputNodeCount = nextLayer.getInputNodeCount();
		stimuliErrors = new float[outputNodeCount];
	}
	
	public void backPropagateError() {
		for (int i = 0; i < stimuliErrors.length; i++)
			stimuliErrors[i] = activationFunction.derivate(stimuli[i]) * nextLayer.getInputError(i);
	}
	
	@Override
	public float getInputError(final int inputIndex) {
		final float[] weightsRow = weights[inputIndex];
		float error = 0.0f;
		
		for (int i = 0; i < stimuliErrors.length; i++)
			error += weightsRow[i] * stimuliErrors[i];
		
		return error;
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		Arrays.fill(inputs, 0);
		Arrays.fill(stimuliErrors, 0);
	}

	@Override
	public void addInput(final int index, final float value) {
		super.addInput(index, value);
		
		
		inputs[index] += value;
	}
	
	@Override
	public int getOutputNodeCount() {
		return stimuliErrors.length;
	}
	
	@Override
	public void updateWeight(final int inputIndex, final int outputIndex, final float step) {
		weights[inputIndex][outputIndex] -= step * stimuliErrors[outputIndex] * inputs[inputIndex];
	}

	@Override
	public void updateBias(final int outputIndex, final float step) {
		biases[outputIndex] -= step * stimuliErrors[outputIndex];
	}
	
	@Override
	public void setWeight(final int inputIndex, final int outputIndex, final float value) {
		weights[inputIndex][outputIndex] = value;
	}
	
	@Override
	public void setBias(final int outputIndex, final float value) {
		biases[outputIndex] = value;
	}

}