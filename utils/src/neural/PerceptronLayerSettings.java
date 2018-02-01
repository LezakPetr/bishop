package neural;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import utils.IoUtils;

public class PerceptronLayerSettings {
	public final IActivationFunction activationFunction;
	public final float[][] weights;
	public final float[] biases;

	public PerceptronLayerSettings(final IActivationFunction activationFunction, final int inputNodeCount, final int outputNodeCount) {
		if (inputNodeCount < 1)
			throw new RuntimeException("Invalid inputNodeCount: " + inputNodeCount);

		if (outputNodeCount < 1)
			throw new RuntimeException("Invalid outputNodeCount: " + outputNodeCount);

		this.activationFunction = activationFunction;
		this.weights = new float[inputNodeCount][outputNodeCount];
		this.biases = new float[outputNodeCount];
	}

	public int getInputNodeCount() {
		return weights.length;
	}
	
	public int getOutputNodeCount() {
		return biases.length;
	}

	public void read(InputStream stream) throws IOException {
		for (float[] weightRow: weights) {
			for (int i = 0; i < weightRow.length; i++)
				weightRow[i] = IoUtils.readFloatBinary (stream);
		}

		for (int i = 0; i < biases.length; i++)
			biases[i] = IoUtils.readFloatBinary (stream);
	}

	public void write(OutputStream stream) throws IOException {
		for (float[] weightRow: weights) {
			for (int i = 0; i < weightRow.length; i++)
				IoUtils.writeFloatBinary (stream, weightRow[i]);
		}

		for (int i = 0; i < biases.length; i++)
			IoUtils.writeFloatBinary (stream, biases[i]);
	}

	public void multiply(final float coeff) {
		for (float[] weightRow: weights) {
			for (int i = 0; i < weightRow.length; i++)
				weightRow[i] *= coeff;
		}

		for (int i = 0; i < biases.length; i++)
			biases[i] *= coeff;
	}

}
