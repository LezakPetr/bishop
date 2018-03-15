package bishop.base;

public class LegalMoveFinder {

	private final IMoveWalker walker = new IMoveWalker() {
		public boolean processMove (final Move move) {
			legalMoveFound = true;
			return false;
		}
	};
	
	private LegalMoveGenerator generator;
	private boolean legalMoveFound;

	public LegalMoveFinder() {
		this(false);
	}

	public LegalMoveFinder(final boolean reduceMovesInCheck) {
		generator = new LegalMoveGenerator();
		generator.setWalker(walker);
		generator.setReduceMovesInCheck(reduceMovesInCheck);
	}
	
	public boolean existsLegalMove (final Position position) {
		legalMoveFound = false;
		
		generator.setPosition(position);
		generator.generateMoves();
		generator.setPosition(null);
		
		return legalMoveFound;
	}
}
