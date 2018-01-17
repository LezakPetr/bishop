package neural;

import java.util.ArrayList;
import java.util.List;

public class PerceptronNetwork extends PerceptronNetworkBase<IPerceptronLayer, IPerceptronLayer, OutputPerceptronLayer> {

	protected PerceptronNetwork(final List<IPerceptronLayer> innerLayers, final OutputPerceptronLayer outputLayer) {
		super(innerLayers, outputLayer);
	}
	
	public static PerceptronNetwork create (final IActivationFunction activationFunction, final int[] sizes) {
		final OutputPerceptronLayer outputLayer = new OutputPerceptronLayer(sizes[sizes.length - 1]);
		final List<IPerceptronLayer> innerLayers = new ArrayList<>();
		IPerceptronLayer lastLayer = outputLayer;
		
		for (int i = sizes.length - 2; i >= 0; i--) {
			final IPerceptronLayer layer = new InnerPerceptronLayer<IPerceptronLayer>(activationFunction, sizes[i], lastLayer);
			innerLayers.add(0, layer);
			lastLayer = layer;
		}
		
		return new PerceptronNetwork(innerLayers, outputLayer);
	}
}