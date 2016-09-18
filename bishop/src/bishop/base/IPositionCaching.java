package bishop.base;

public interface IPositionCaching {

	public void movePiece(final int color, final int pieceType, final int beginSquare, final int targetSquare);
	public void addPiece(final int color, final int pieceType, final int square);
	public void removePiece(final int color, final int pieceType, final int square);
	public void swapOnTurn();
	public void changeEpFile(final int from, final int to);
	public void changeCastlingRights(final int fromIndex, final int toIndex);
	public long getHash();
	public void setHash(final long hash);
	public void refreshHash(final Position position);
	public MaterialHash getMaterialHash();
	public void setMaterialHash(final MaterialHash hash);
	public void refreshMaterialHash(final Position position);
	public IPositionCaching copy();
}
