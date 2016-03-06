package bishop.base;

import bishop.tables.PieceHashTable;

public final class PositionCachingImpl implements IPositionCaching {

	private long hash;
	private int materialEvaluation;

	public void movePiece(final int color, final int pieceType, final int beginSquare, final int targetSquare) {
		hash ^= PieceHashTable.getItem(color, pieceType, beginSquare);
		hash ^= PieceHashTable.getItem(color, pieceType, targetSquare);
	}
	
	public void addPiece(final int color, final int pieceType, final int square) {
		hash ^= PieceHashTable.getItem(color, pieceType, square);
		materialEvaluation += PieceTypeEvaluations.getPieceEvaluation(color, pieceType);		
	}
	
	public void removePiece(final int color, final int pieceType, final int square) {
		hash ^= PieceHashTable.getItem(color, pieceType, square);
		materialEvaluation -= PieceTypeEvaluations.getPieceEvaluation(color, pieceType);
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
	public int getMaterialEvaluation() {
		return materialEvaluation;
	}

	@Override
	public void setMaterialEvaluation(final int evaluation) {
		this.materialEvaluation = evaluation;
	}
	
	@Override
	public void refreshMaterialEvaluation(final Position position) {
		setMaterialEvaluation(position.calculateMaterialEvaluation());
	}

	@Override
	public IPositionCaching copy() {
		final PositionCachingImpl result = new PositionCachingImpl();
		result.hash = hash;
		result.materialEvaluation = materialEvaluation;
		
		return result;
	}
}
