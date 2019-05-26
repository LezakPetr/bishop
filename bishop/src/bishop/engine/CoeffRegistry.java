package bishop.engine;

import math.IVector;
import math.IVectorRead;
import math.Vectors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoeffRegistry {
	
	private final List<String> coeffNames = new ArrayList<>();
	private final List<String> nameStack = new ArrayList<>();
	private final List<IVectorRead> coeffFeatures = new ArrayList<>();
	private int featureCount;

	private boolean frozen;

	public int addCoeff(final String name) {
		checkNotFrozen();

		final int feature = addFeature();

		return addCoeffWithFeatures (name, Vectors.getUnitVector(feature, featureCount));
	}

	public int addCoeffWithFeatures (final String name, final IVectorRead features) {
		checkNotFrozen();
		
		final int coeff = coeffNames.size();
		
		nameStack.add(name);
		
		final String fullName = nameStack.stream().collect(Collectors.joining("."));
		coeffNames.add(fullName);
		coeffFeatures.add(features);
		
		nameStack.remove(nameStack.size() - 1);
		
		return coeff;
	}

	public int addFeature() {
		checkNotFrozen();

		final int feature = featureCount;
		featureCount++;

		return feature;
	}
	
	public int enterCategory(final String name) {
		checkNotFrozen();
		
		nameStack.add(name);
		
		return coeffNames.size();
	}
	
	public int leaveCategory() {
		checkNotFrozen();
		
		nameStack.remove(nameStack.size() - 1);
		
		return coeffNames.size();
	}
	
	public String getName (final int coeff) {
		return coeffNames.get(coeff);
	}

	public int finish() {
		checkNotFrozen();
		
		if (!nameStack.isEmpty())
			throw new RuntimeException("Name stack is not empty");
		
		frozen = true;
		resizeFeatureVectors();

		return coeffNames.size();
	}

	private void resizeFeatureVectors() {
		coeffFeatures.replaceAll(
				f -> {
					final IVector v = Vectors.sparse(featureCount);
					v.subVector(0, f.getDimension()).assign(f);

					return v.freeze();
				}
		);
	}

	private void checkNotFrozen() {
		if (frozen)
			throw new RuntimeException("Coeff registry is frozen");
	}

	public int getFeatureCount() {
		return featureCount;
	}

	public IVectorRead getFeaturesOfCoeff (final int coeff) {
		return coeffFeatures.get(coeff);
	}

}
