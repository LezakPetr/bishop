package neural;

import java.util.Arrays;

public class Sample {
	private final float[] input;
	private final float[] output;
	private final float weight;
	
	public Sample(final float[] input, final float[] output, final float weight) {
		this.input = Arrays.copyOf(input, input.length);
		this.output = Arrays.copyOf(output, output.length);
		this.weight = weight;
	}

	public float getInput(final int index) {
		return input[index];
	}

	public float getOutput(final int index) {
		return output[index];
	}

	public float getWeight() {
		return weight;
	}

}
