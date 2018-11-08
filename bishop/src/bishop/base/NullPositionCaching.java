package bishop.base;

import bishop.engine.CombinedEvaluation;

public class NullPositionCaching implements IPositionCaching {

	private final Position position;
	private CombinedPositionEvaluationTable evaluationTable;
	
	public NullPositionCaching (final Position position) {
		this.position = position;
		this.evaluationTable = CombinedPositionEvaluationTable.ZERO_TABLE;
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
	public void refreshCache(final Position position) {
	}

	@Override
	public void assign (final IPositionCaching orig) {
	}

	@Override
	public MaterialHash getMaterialHash() {
		return position.calculateMaterialHash();
	}

	@Override
	public long getCombinedEvaluation() {
		return calculateCombinedEvaluation(position, evaluationTable);
	}

	@Override
	public int getTablePositionEvaluation(final int gameStage) {
		return CombinedEvaluation.decode(
				calculateCombinedEvaluation(position, evaluationTable),
				CombinedEvaluation.getMultiplicatorForGameStage(gameStage)
		);
	}

	public static long calculateCombinedEvaluation(final Position position, final CombinedPositionEvaluationTable evaluationTable) {
		long combinedEvaluation = CombinedEvaluation.ACCUMULATOR_BASE;

		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				for (BitLoop loop = new BitLoop(position.getPiecesMask(color, pieceType)); loop.hasNextSquare(); ) {
					final int square = loop.getNextSquare();

					combinedEvaluation += evaluationTable.getCombinedEvaluation(color, pieceType, square);
				}
			}
		}

		return combinedEvaluation;
	}

	@Override
	public CombinedPositionEvaluationTable getCombinedPositionEvaluationTable() {
		return evaluationTable;
	}

	@Override
	public void setCombinedPositionEvaluationTable(final CombinedPositionEvaluationTable table) {
		this.evaluationTable = table;
	}

}
