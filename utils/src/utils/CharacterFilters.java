package utils;

public class CharacterFilters {
	
	private static final String END_OF_LINE_CHARACTERS = "\n\r";
	
	private static final ICharacterFilter whiteSpaceFilter = new WhiteSpaceFilter();
	private static final ICharacterFilter nonWhiteSpaceFilter = new NegateCharacterFilter(whiteSpaceFilter);

	private static final ICharacterFilter endOfLineFilter = new DefinedCharacterFilter(END_OF_LINE_CHARACTERS);
	private static final ICharacterFilter nonEndOfLineFilter = new NegateCharacterFilter(endOfLineFilter);

	public static ICharacterFilter getWhiteSpaceFilter() {
		return whiteSpaceFilter;
	}

	public static ICharacterFilter getNonWhiteSpaceFilter() {
		return nonWhiteSpaceFilter;
	}

	public static ICharacterFilter getEndOfLineFilter() {
		return endOfLineFilter;
	}

	public static ICharacterFilter getNonEndOfLineFilter() {
		return nonEndOfLineFilter;
	}

}
