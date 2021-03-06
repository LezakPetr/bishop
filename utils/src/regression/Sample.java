package regression;

import math.IVectorRead;
import math.Vectors;


public class Sample implements ISample {
	private final IVectorRead input;
	private final IVectorRead output;
	private final double weight;

	public Sample(final IVectorRead input, final IVectorRead output, final double weight) {
		this.input = input.immutableCopy();
		this.output = output.immutableCopy();
		this.weight = weight;
	}
	
	public Sample(final float[] input, final float[] output, final float weight) {
		this.input = Vectors.of(input);
		this.output = Vectors.of(output);
		this.weight = weight;
	}

	@Override
	public IVectorRead getInput() {
		return input;
	}

	@Override
	public IVectorRead getOutput() {
		return output;
	}

	@Override
	public double getWeight() {
		return weight;
	}

}
