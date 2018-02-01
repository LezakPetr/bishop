package neural;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import utils.IoUtils;

public class PerceptronNetworkSettings {

	private final List<PerceptronLayerSettings> innerLayers = new ArrayList<>();
	
	public int getInnerLayerCount() {
		return innerLayers.size();
	}
	
	public PerceptronLayerSettings getInnerLayerAt (final int index) {
		return innerLayers.get(index);
	}
	
	public void addLayer (final PerceptronLayerSettings settings) {
		this.innerLayers.add(settings);
	}
	
	public static PerceptronNetworkSettings createEmpty(final BiFunction<Integer, Integer, IActivationFunction> activationFunctionSupplier, final int[] layerSizes) {
		final PerceptronNetworkSettings networkSettings = new PerceptronNetworkSettings();
		networkSettings.fillEmptyLayers(activationFunctionSupplier, layerSizes);
		
		return networkSettings;
	}
	
	private void fillEmptyLayers(final BiFunction<Integer, Integer, IActivationFunction> activationFunctionSupplier, final int[] layerSizes) {
		for (int i = 0; i < layerSizes.length - 1; i++) {
			final IActivationFunction activationFunction = activationFunctionSupplier.apply(i, layerSizes.length - 1);
			final PerceptronLayerSettings layerSettings = new PerceptronLayerSettings(activationFunction, layerSizes[i], layerSizes[i+1]);
			innerLayers.add(layerSettings);
		}		
	}

	public void read(final InputStream stream, final BiFunction<Integer, Integer, IActivationFunction> activationFunctionSupplier) throws IOException {
		// Count of layers
		final int layerCount = (int) IoUtils.readUnsignedNumberBinary(stream, IoUtils.SHORT_BYTES);
		
		// Sizes of layers
		final int[] layerSizes = new int[layerCount];
		
		for (int i = 0; i < layerCount; i++)
			layerSizes[i] = (int) IoUtils.readSignedNumberBinary(stream, IoUtils.INT_BYTES);
		
		// Layers
		fillEmptyLayers(activationFunctionSupplier, layerSizes);
		
		for (PerceptronLayerSettings layer: innerLayers)
			layer.read(stream);
	}

	public void write(final OutputStream stream) throws IOException {
		// Count of layers
		final int layerCount = innerLayers.size() + 1;
		IoUtils.writeNumberBinary(stream, layerCount, IoUtils.SHORT_BYTES);
		
		// Sizes of layers
		for (PerceptronLayerSettings layer: innerLayers)
			IoUtils.writeNumberBinary(stream, layer.getInputNodeCount(), IoUtils.INT_BYTES);
		
		final int outputNodeCount = innerLayers.get(innerLayers.size() - 1).getOutputNodeCount();
		IoUtils.writeNumberBinary(stream, outputNodeCount, IoUtils.INT_BYTES);
		
		// Layers
		for (PerceptronLayerSettings layer: innerLayers)
			layer.write(stream);
	}

}
