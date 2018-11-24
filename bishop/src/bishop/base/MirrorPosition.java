package bishop.base;

public class MirrorPosition implements IPosition {

	private final IPosition basePosition;
	
	public MirrorPosition(final IPosition basePosition) {
		this.basePosition = basePosition;
	}
	
	@Override
	public int getPieceCount(final int color, final int pieceType) {
		final int oppositeColor = Color.getOppositeColor(color);
		
		return basePosition.getPieceCount(oppositeColor, pieceType);
	}

	@Override
	public int getKingPosition(final int color) {
		final int oppositeColor = Color.getOppositeColor(color);
		final int kingPosition = basePosition.getKingPosition(oppositeColor);
		
		return Square.getOppositeSquare(kingPosition);
	}

	@Override
	public int getEpFile() {
		return basePosition.getEpFile();
	}

	@Override
	public long getOccupancy() {
		return BitBoard.getMirrorBoard (basePosition.getOccupancy());
	}

	@Override
	public int getOnTurn() {
		return Color.getOppositeColor(basePosition.getOnTurn());
	}

	@Override
	public long getPiecesMask(final int color, final int type) {
		final int oppositeColor = Color.getOppositeColor(color);

		return BitBoard.getMirrorBoard (basePosition.getPiecesMask(oppositeColor, type));
	}

	@Override
	public MaterialHash getMaterialHash() {
		return basePosition.getMaterialHash().getOpposite();
	}

	@Override
	public long getColorOccupancy(final int color) {
		final int oppositeColor = Color.getOppositeColor(color);

		return BitBoard.getMirrorBoard (basePosition.getColorOccupancy(oppositeColor));
	}

	@Override
	public CastlingRights getCastlingRights() {
		final CastlingRights rights = new CastlingRights();
		rights.assignMirror(basePosition.getCastlingRights());
		
		return rights;
	}

	@Override
	public int getTablePositionEvaluation (final int gameStage) {
		return -basePosition.getTablePositionEvaluation(gameStage);
	}

	@Override
	public void setCombinedPositionEvaluationTable(CombinedPositionEvaluationTable table) {

	}

	@Override
	public void setPieceTypeEvaluations(PieceTypeEvaluations pieceTypeEvaluations) {

	}

	@Override
	public int getMaterialEvaluation() {
		return -basePosition.getMaterialEvaluation();
	}

	@Override
	public int getGameStage() {
		return basePosition.getGameStage();
	}

}
