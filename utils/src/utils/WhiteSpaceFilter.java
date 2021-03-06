package utils;

public final class WhiteSpaceFilter implements ICharacterFilter {
	
	/**
	 * Filters given character.
	 * @param ch filtered character
	 * @return true if character matches this filter, false if not
	 */
	public boolean filterCharacter (final char ch) {
		return Character.isWhitespace(ch);
	}
	
}
