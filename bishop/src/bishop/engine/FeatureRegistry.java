package bishop.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureRegistry {
	
	private final List<String> featureNames = new ArrayList<>();
	private final List<String> nameStack = new ArrayList<>();
	private boolean frozen;
	
	public short add (final String name) {
		checkNotFrozen();
		
		final int feature = featureNames.size();
		
		nameStack.add(name);
		
		final String fullName = nameStack.stream().collect(Collectors.joining("."));
		featureNames.add(fullName);
		
		nameStack.remove(nameStack.size() - 1);
		
		return (short) feature;
	}
	
	public short enterCategory(final String name) {
		checkNotFrozen();
		
		nameStack.add(name);
		
		return (short) featureNames.size();
	}
	
	public short leaveCategory() {
		checkNotFrozen();
		
		nameStack.remove(nameStack.size() - 1);
		
		return (short) featureNames.size();
	}
	
	public String getName (final int coeff) {
		return featureNames.get(coeff);
	}

	public short finish() {
		checkNotFrozen();
		
		if (!nameStack.isEmpty())
			throw new RuntimeException("Name stack is not empty");
		
		frozen = true;
		
		return (short) featureNames.size();
	}
	
	private void checkNotFrozen() {
		if (frozen)
			throw new RuntimeException("Coeff registry is frozen");
	}
}
