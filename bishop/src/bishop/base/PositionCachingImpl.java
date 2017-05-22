package bishop.base;

import bishop.tables.PieceHashTable;

public final class PositionCachingImpl implements IPositionCaching {

	private long hash;
	private final MaterialHash materialHash = new MaterialHash();

	public void movePiece(final int color, final int pieceType, final int beginSquare, final int targetSquare) {
		hash ^= PieceHashTable.getItem(color, pieceType, beginSquare);
		hash ^= PieceHashTable.getItem(color, pieceType, targetSquare);
	}
	
	public void addPiece(final int color, final int pieceType, final int square) {
		hash ^= PieceHashTable.getItem(color, pieceType, square);
		materialHash.addPiece(color, pieceType);
	}
	
	public void removePiece(final int color, final int pieceType, final int square) {
		hash ^= PieceHashTable.getItem(color, pieceType, square);
		materialHash.removePiece(color, pieceType);
	}
	
	public void swapOnTurn() {
		hash ^= HashConstants.getOnTurnHashDifference();
		materialHash.swapOnTurn();
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
	public void refreshCache(final Position position) {
		hash = position.calculateHash();
		materialHash.assign(position.calculateMaterialHash());
	}
	
	@Override
	public MaterialHash getMaterialHash() {
		return materialHash;
	}

	@Override
	public void assign (final IPositionCaching orig) {
		this.hash = orig.getHash();
		this.materialHash.assign(orig.getMaterialHash());
	}

}
