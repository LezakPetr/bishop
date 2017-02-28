package bishop.base;

import bishop.tables.PieceHashTable;

public final class PositionCachingImpl implements IPositionCaching {

	private long hash;

	public void movePiece(final int color, final int pieceType, final int beginSquare, final int targetSquare) {
		hash ^= PieceHashTable.getItem(color, pieceType, beginSquare);
		hash ^= PieceHashTable.getItem(color, pieceType, targetSquare);
	}
	
	public void addPiece(final int color, final int pieceType, final int square) {
		hash ^= PieceHashTable.getItem(color, pieceType, square);
	}
	
	public void removePiece(final int color, final int pieceType, final int square) {
		hash ^= PieceHashTable.getItem(color, pieceType, square);
	}
	
	public void swapOnTurn() {
		hash ^= HashConstants.getOnTurnHashDifference();
	}
	
	public void changeEpFile(final int from, final int to) {
		if (from != to) {
			hash ^= HashConstants.getEpFileHash(from);
			hash ^= HashConstants.getEpFileHash(to);
		}
	}

	public void changeCastlingRights(final int fromIndex, final int toIndex) {
		if (fromIndex != toIndex) {
			hash ^= HashConstants.getCastlingRightHash(fromIndex);
			hash ^= HashConstants.getCastlingRightHash(toIndex);
		}
	}

	@Override
	public long getHash() {
		return hash;
	}

	@Override
	public void setHash(final long hash) {
		this.hash = hash;
	}
	
	@Override
	public void refreshHash(final Position position) {
		setHash(position.calculateHash());
	}

	@Override
	public void assign (final IPositionCaching orig) {
		this.hash = orig.getHash();
	}

}
