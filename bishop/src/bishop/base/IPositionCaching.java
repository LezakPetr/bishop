package bishop.base;

public interface IPositionCaching {

	public void movePiece(final int color, final int pieceType, final int beginSquare, final int targetSquare);
	public void addPiece(final int color, final int pieceType, final int square);
	public void removePiece(final int color, final int pieceType, final int square);
	public void swapOnTurn();
	public void changeEpFile(final int from, final int to);
	public void changeCastlingRights(final int fromIndex, final int toIndex);
	public long getHash();
	public void refreshCache(final Position position);
	public void assign (final IPositionCaching orig);
	public MaterialHash getMaterialHash();
	public int getTablePositionEvaluation(final int gameStage);
	public long getCombinedEvaluation();
	public CombinedPositionEvaluationTable getCombinedPositionEvaluationTable();
	public void setCombinedPositionEvaluationTable(final CombinedPositionEvaluationTable table);
	public PieceTypeEvaluations getPieceTypeEvaluations();
	public void setPieceTypeEvaluations (final PieceTypeEvaluations pieceTypeEvaluations);
	public int getMaterialEvaluation();
	public int getGameStageUnbound();
}
