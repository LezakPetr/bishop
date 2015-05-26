package utils;

public final class NegateCharacterFilter implements ICharacterFilter {
	
	private final ICharacterFilter baseFilter;
	
	
	public NegateCharacterFilter(final ICharacterFilter baseFilter) {
		this.baseFilter = baseFilter;
	}

	@Override
	public boolean filterCharacter(char ch) {
		return !baseFilter.filterCharacter(ch);
	}
	

}
