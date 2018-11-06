package regression;

import collections.ImmutableEnumSet;
import math.IMatrixRead;
import math.IVectorRead;
import math.Matrices;
import math.Vectors;

public class DirectFeatureCombination implements ISampleCostField {

	private final int featureCount;
	private final IMatrixRead hessian;

	public DirectFeatureCombination(final int featureCount) {
		this.featureCount = featureCount;
		this.hessian = Matrices.getZeroMatrix(featureCount, featureCount);
	}

	@Override
	public int getInputDimension() {
		return featureCount;
	}

	@Override
	public ScalarPointCharacteristics calculate(final IVectorRead x, final ISample sample, final ImmutableEnumSet<ScalarFieldCharacteristic> characteristics) {
		final IVectorRead gradient = sample.getInput();

		return new ScalarPointCharacteristics(
				() -> gradient.dotProduct(x),
				() -> gradient,
				() -> hessian,
				characteristics
		);
	}


}
