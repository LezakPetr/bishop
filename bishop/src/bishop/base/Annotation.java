package bishop.base;

public enum Annotation {
	
	NONE (0, ""),
	GOOD_MOVE (1, "!"),
	POOR_MOVE (2, "?"),
	VERY_GOOD_MOVE (3, "!!"),
	VERY_POOR_MOVE (4, "??"),
	SPECULATIVE_MOVE (5, "!?"),
	QUESTIONABLE_MOVE (6, "?!"),
	WHITE_CRUISING (20, "+--"),
	BLACK_CRUISING (21, "--+"),
	WHITE_HAS_DECISIVE_ADVANTAGE (18, "+-"),
	BLACK_HAS_DECISIVE_ADVANTAGE (19, "-+"),
	WHITE_HAS_MODERATE_ADVANTAGE (16, "+/-"),
	BLACK_HAS_MODERATE_ADVANTAGE (17, "-/+"),
	WHITE_HAS_SLIGHT_ADVANTAGE (14, "+="),
	BLACK_HAS_SLIGHT_ADVANTAGE (15, "-="),
	DRAWISH_POSITION (10, "="),
	UNCLEAR_POSITION (13, "~");
	
	
	public boolean isGoodMove() {
		return this != POOR_MOVE && this != VERY_POOR_MOVE;
	}
	
	private final int nag;
	private final String symbol;
	
	private Annotation (final int nag, final String symbol) {
		this.nag = nag;
		this.symbol = symbol;
	}

	public int getNag() {
		return nag;
	}

	public String getSymbol() {
		return symbol;
	}
	
	@Override
	public String toString() {
		return symbol;
	}

	public static Annotation withNag(final int nag) {
		for (Annotation annotation: Annotation.values()) {
			if (annotation.nag == nag)
				return annotation;
		}
		
		return Annotation.NONE;
	}
}
