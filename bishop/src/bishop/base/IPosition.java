package bishop.base;

public interface IPosition extends IPieceCounts {
	public int getKingPosition (final int color);
	public int getEpFile();
	public long getOccupancy();
	public long getColorOccupancy (final int color);
	public long getPiecesMask (final int color, final int type);
	public int getOnTurn();
	public MaterialHash getMaterialHash();
	public CastlingRights getCastlingRights();
}
