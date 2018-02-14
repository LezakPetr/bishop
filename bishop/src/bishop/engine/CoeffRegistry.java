package bishop.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CoeffRegistry {
	
	private final List<String> coeffNames = new ArrayList<>();
	private final List<String> nameStack = new ArrayList<>();
	private final List<CoeffLink> coeffLinks = new ArrayList<>();
	private boolean frozen;
	
	public short add (final String name) {
		checkNotFrozen();
		
		final int coeff = coeffNames.size();
		
		nameStack.add(name);
		
		final String fullName = nameStack.stream().collect(Collectors.joining("."));
		coeffNames.add(fullName);
		
		nameStack.remove(nameStack.size() - 1);
		
		return (short) coeff;
	}
	
	public void addLink(final CoeffLink link) {
		checkNotFrozen();
		
		coeffLinks.add(link);
	}
	
	public short enterCategory(final String name) {
		checkNotFrozen();
		
		nameStack.add(name);
		
		return (short) coeffNames.size();
	}
	
	public short leaveCategory() {
		checkNotFrozen();
		
		nameStack.remove(nameStack.size() - 1);
		
		return (short) coeffNames.size();
	}
	
	public String getName (final int coeff) {
		return coeffNames.get(coeff);
	}

	public short finish() {
		checkNotFrozen();
		
		if (!nameStack.isEmpty())
			throw new RuntimeException("Name stack is not empty");
		
		frozen = true;
		
		return (short) coeffNames.size();
	}
	
	private void checkNotFrozen() {
		if (frozen)
			throw new RuntimeException("Coeff registry is frozen");
	}
	
	public List<CoeffLink> getCoeffLinks() {
		return Collections.unmodifiableList(coeffLinks);
	}
}
