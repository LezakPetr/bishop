package neural;

import java.util.List;

import collections.ImmutableList;

public class PerceptronNetworkBase<L extends IPerceptronLayer, I extends L, O extends L> implements IPerceptronLayer {

	protected final List<I> innerLayers;
	protected final O outputLayer;
	protected final List<L> allLayers;
	
	protected PerceptronNetworkBase(final List<I> innerLayers, final O outputLayer) {
		this.innerLayers = ImmutableList.copyOf (innerLayers);
		this.outputLayer = outputLayer;
		
		this.allLayers = ImmutableList.<L>builder()
				.withCapacity(innerLayers.size() + 1)
				.addAll(innerLayers)
				.add(outputLayer)
				.build();
	}

	@Override
	public int getInputNodeCount() {
		return getInputLayer().getInputNodeCount();
	}

	protected L getInputLayer() {
		return innerLayers.get(0);
	}

	public int getOutputNodeCount() {
		return outputLayer.getInputNodeCount();
	}

	@Override
	public void initialize() {
		for (L layer: allLayers)
			layer.initialize();
	}

	@Override
	public void addInput(final int index, final float value) {
		for (L layer: allLayers)
			layer.initialize();
	}

	@Override
	public void propagate() {
		for (L layer: allLayers)
			layer.propagate();
	}
	
}
