package bishop.base;

public enum GameResult {
	
	WHITE_WINS ("1-0"),
	BLACK_WINS ("0-1"),
	DRAW ("1/2-1/2"),
	GAME_IN_PROGRESS ("*");

	
	private final String name;
	
	private GameResult (final String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

	public static GameResult fromString(final String str) {
		for (GameResult result: GameResult.values()) {
			if (str.equalsIgnoreCase(result.toString()))
				return result;
		}
		
		throw new RuntimeException("String '" + str + "' does not match to any game result");
	}

}
