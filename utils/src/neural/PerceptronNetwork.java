package neural;

import java.util.ArrayList;
import java.util.List;

public class PerceptronNetwork extends PerceptronNetworkBase<IPerceptronLayer, IPerceptronLayer, OutputPerceptronLayer> {

	protected PerceptronNetwork(final List<IPerceptronLayer> innerLayers, final OutputPerceptronLayer outputLayer) {
		super(innerLayers, outputLayer);
	}
	
	public static PerceptronNetwork create (final PerceptronNetworkSettings networkSettings) {
		final int innerLayerCount = networkSettings.getInnerLayerCount();
		final PerceptronLayerSettings lastInnerLayerSettings = networkSettings.getInnerLayerAt(innerLayerCount - 1);
		final OutputPerceptronLayer outputLayer = new OutputPerceptronLayer(lastInnerLayerSettings.getOutputNodeCount());
		final List<IPerceptronLayer> innerLayers = new ArrayList<>();
		IPerceptronLayer previousLayer = outputLayer;
		
		for (int i = innerLayerCount - 1; i >= 0; i--) {
			final PerceptronLayerSettings layerSettings = networkSettings.getInnerLayerAt(i);
			final IPerceptronLayer layer = new InnerPerceptronLayer<IPerceptronLayer>(layerSettings, previousLayer);
			innerLayers.add(0, layer);
			previousLayer = layer;
		}
		
		return new PerceptronNetwork(innerLayers, outputLayer);
	}

	public float getOutput(final int index) {
		return outputLayer.getInput (index);
	}

}