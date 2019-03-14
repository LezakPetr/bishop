package bishop.base;

public class LegalMoveFinder {

	private final LegalMoveGenerator generator;
	private boolean legalMoveFound;

	public LegalMoveFinder() {
		this(false);
	}

	public LegalMoveFinder(final boolean reduceMovesInCheck) {
		generator = new LegalMoveGenerator();
		generator.setWalker(this::processMove);
		generator.setReduceMovesInCheck(reduceMovesInCheck);
	}

	private boolean processMove (final Move move) {
		legalMoveFound = true;
		return false;
	}

	public boolean existsLegalMove (final Position position) {
		legalMoveFound = false;
		
		generator.setPosition(position);
		generator.generateMoves();
		generator.setPosition(null);
		
		return legalMoveFound;
	}
}
