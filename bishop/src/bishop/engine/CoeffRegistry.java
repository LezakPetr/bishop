package bishop.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CoeffRegistry {
	
	private final List<String> coeffNames = new ArrayList<>();
	private final List<String> nameStack = new ArrayList<>();
	private boolean frozen;
	
	public int add (final String name) {
		checkNotFrozen();
		
		final int coeff = coeffNames.size();
		
		nameStack.add(name);
		
		final String fullName = nameStack.stream().collect(Collectors.joining("."));
		coeffNames.add(fullName);
		
		nameStack.remove(nameStack.size() - 1);
		
		return coeff;
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
		
		return coeffNames.size();
	}
	
	private void checkNotFrozen() {
		if (frozen)
			throw new RuntimeException("Coeff registry is frozen");
	}

}
