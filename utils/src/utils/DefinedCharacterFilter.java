package utils;

public final class DefinedCharacterFilter implements ICharacterFilter {
	
	private final String characters;
	
	public DefinedCharacterFilter(final String characters) {
		this.characters = characters;
	}
	
	/**
	 * Filters given character.
	 * @param ch filtered character
	 * @return true if character matches this filter, false if not
	 */
	public boolean filterCharacter (final char ch) {
		return characters.indexOf(ch) >= 0;
	}

}
