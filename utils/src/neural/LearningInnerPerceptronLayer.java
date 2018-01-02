package neural;

public class LearningInnerPerceptronLayer extends InnerPerceptronLayer<ILearningPerceptronLayer> implements ILearningPerceptronLayer {
	
	private final float[] stimuliErrors; 
			
	public LearningInnerPerceptronLayer (final IActivationFunction activationFunction, final int inputNodeCount, final ILearningPerceptronLayer nextLayer) {
		super (activationFunction, inputNodeCount, nextLayer);
		
		final int outputNodeCount = getOutputNodeCount();
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
}
