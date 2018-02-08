package neural;

import java.util.Arrays;

public class Sample implements ISample{
	private final int[] inputIndices;
	private final float[] inputValues;
	private final float[] output;
	private final float weight;
	
	public Sample(final int[] inputIndices, final float[] inputValues, final float[] output, final float weight) {
		if (inputIndices.length != inputValues.length)
			throw new RuntimeException("Different lengths of inputs");
		
		this.inputIndices = Arrays.copyOf(inputIndices, inputIndices.length);
		this.inputValues = Arrays.copyOf(inputValues, inputValues.length);
		this.output = Arrays.copyOf(output, output.length);
		this.weight = weight;
	}

	@Override
	public int getInputIndex(final int nonZeroIndex) {
		return inputIndices[nonZeroIndex];
	}

	@Override
	public float getInputValue(final int nonZeroIndex) {
		return inputValues[nonZeroIndex];
	}
	
	@Override
	public int getNonZeroInputCount() {
		return inputIndices.length;
	}

	@Override
	public float getOutput(final int index) {
		return output[index];
	}

	@Override
	public float getWeight() {
		return weight;
	}

}
