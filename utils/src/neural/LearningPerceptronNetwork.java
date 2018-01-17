package neural;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LearningPerceptronNetwork extends PerceptronNetworkBase<ILearningPerceptronLayer, ILearningInnerPerceptronLayer, LearningOutputPerceptronLayer> implements ILearningPerceptronLayer {
	
	protected LearningPerceptronNetwork(final List<ILearningInnerPerceptronLayer> innerLayers, final LearningOutputPerceptronLayer outputLayer) {
		super(innerLayers, outputLayer);
	}
	
	@Override
	public void backPropagateError() {
		for (int i = allLayers.size() - 1; i >= 0; i--)
			allLayers.get(i).backPropagateError();
	}

	@Override
	public float getInputError(final int inputIndex) {
		return getInputLayer().getInputError(inputIndex);
	}
	
	public static LearningPerceptronNetwork create (final IActivationFunction activationFunction, final int[] sizes) {
		final LearningOutputPerceptronLayer outputLayer = new LearningOutputPerceptronLayer(sizes[sizes.length - 1]);
		final List<ILearningInnerPerceptronLayer> innerLayers = new ArrayList<>();
		ILearningPerceptronLayer lastLayer = outputLayer;
		
		for (int i = sizes.length - 2; i >= 0; i--) {
			final ILearningInnerPerceptronLayer layer = new LearningInnerPerceptronLayer(activationFunction, sizes[i], lastLayer);
			innerLayers.add(0, layer);
			lastLayer = layer;
		}
		
		return new LearningPerceptronNetwork(innerLayers, outputLayer);
	}
	
	
	public void randomInitializeNetwork(final Random rng) {
		for (ILearningInnerPerceptronLayer layer: innerLayers) {
			for (int i = 0; i < layer.getOutputNodeCount(); i++) {
				for (int j = 0; j < layer.getInputNodeCount(); j++)
					layer.setWeight (j, i, (float) rng.nextGaussian());
				
				layer.setBias (i, (float) rng.nextGaussian());
			}
		}
	}

	public void learnFromSample (final Sample sample, final float alpha) {
		propagateSampleAndCalculateError(sample);
		backPropagateError();
		updateWeights(sample.getWeight() * alpha);
	}
	
	public void propagateSampleAndCalculateError (final Sample sample) {
		initialize();
		setSampleInput(sample);
		propagate();
		setSampleExpectedOutput(sample);
	}

	private void setSampleInput(final Sample sample) {
		for (int i = 0; i < getInputNodeCount(); i++)
			getInputLayer().addInput(i, sample.getInput (i));
	}
	
	private void setSampleExpectedOutput(final Sample sample) {
		for (int i = 0; i < getOutputNodeCount(); i++)
			outputLayer.setExpectedInput(i, sample.getOutput (i));
	}
	
	private void updateWeights(final float step) {
		for (ILearningInnerPerceptronLayer layer: innerLayers) {
			for (int i = 0; i < layer.getOutputNodeCount(); i++) {
				for (int j = 0; j < layer.getInputNodeCount(); j++)
					layer.updateWeight (j, i, step);
				
				layer.updateBias (i, step);
			}
		}
	}

	public double getOutputError() {
		return outputLayer.getTotalInputError();
	}
}
