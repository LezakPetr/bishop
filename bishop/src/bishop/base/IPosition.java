package bishop.base;

public interface IPosition extends IPieceCounts {
	public int getKingPosition (final int color);
	public int getEpFile();
	public long getOccupancy();
	public long getColorOccupancy (final int color);
	public long getPiecesMask (final int color, final int type);
	public int getOnTurn();
	public IMaterialHashRead getMaterialHash();
	public CastlingRights getCastlingRights();
	public int getTablePositionEvaluation (final int gameStage);
	public void setCombinedPositionEvaluationTable(final CombinedPositionEvaluationTable table);
	public void setPieceTypeEvaluations (final PieceTypeEvaluations pieceTypeEvaluations);
	public int getMaterialEvaluation();
	public int getGameStage();
	public Piece getSquareContent (final int square);
}
