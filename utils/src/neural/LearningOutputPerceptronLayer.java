package neural;

public class LearningOutputPerceptronLayer extends OutputPerceptronLayer implements ILearningPerceptronLayer {

	private final float[] expectedInput;
	
	public LearningOutputPerceptronLayer(final int inputCount) {
		super (inputCount);
		
		this.expectedInput = new float[inputCount];
	}
	
	@Override
	public void backPropagateError() {
	}

	@Override
	public float getInputError(final int inputIndex) {
		return input[inputIndex] - expectedInput[inputIndex];
	}

	public float getInput(final int inputIndex) {
		return input[inputIndex];
	}

	public void setExpectedInput(final int inputIndex, final float value) {
		expectedInput[inputIndex] = value;
	}

	public double getTotalInputError() {
		double error = 0.0;
		
		for (int i = 0; i < expectedInput.length; i++) {
			final double diff = expectedInput[i] - input[i];
			error += diff * diff;
		}
		
		return error;
	}
}
