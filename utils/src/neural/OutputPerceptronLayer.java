package neural;

import java.util.Arrays;

public class OutputPerceptronLayer implements IPerceptronLayer {
	
	protected final float[] input;
	
	public OutputPerceptronLayer(final int inputNodeCount) {
		this.input = new float[inputNodeCount];
	}

	@Override
	public int getInputNodeCount() {
		return input.length;
	}

	@Override
	public void initialize() {
		Arrays.fill(input, 0.0f);
	}

	@Override
	public void addInput(final int inputIndex, final float value) {
		input[inputIndex] += value;
	}

	@Override
	public void propagate() {
	}

}
