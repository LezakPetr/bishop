package bishop.base;

public class NullPositionCaching implements IPositionCaching {

	private final Position position;
	
	public NullPositionCaching (final Position position) {
		this.position = position;
	}
	
	@Override
	public void movePiece(final int color, final int pieceType, final int beginSquare, final int targetSquare) {
	}

	@Override
	public void addPiece(final int color, final int pieceType, final int square) {
	}

	@Override
	public void removePiece(final int color, final int pieceType, final int square) {
	}

	@Override
	public void swapOnTurn() {
	}

	@Override
	public void changeEpFile(final int from, final int to) {
	}

	@Override
	public void changeCastlingRights(final int fromIndex, final int toIndex) {
	}

	@Override
	public long getHash() {
		return position.calculateHash();
	}

	@Override
	public void setHash(final long hash) {
	}
	
	@Override
	public void refreshHash(final Position position) {
	}

	@Override
	public IPositionCaching copy() {
		return new NullPositionCaching(position);
	}

}
