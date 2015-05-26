package bishop.engine;

public final class TablePositionEvaluatorSettings {
	
	private final PieceSquareEvaluationTable pieceEvaluationTable;
	
	public TablePositionEvaluatorSettings() {
		pieceEvaluationTable = new PieceSquareEvaluationTable();
	}

	public PieceSquareEvaluationTable getPieceEvaluationTable() {
		return pieceEvaluationTable;
	}
	
	public void setPieceEvaluationTable (final double[][] squareEvaluation, final double[] pieceTypeCoeffs) {
		pieceEvaluationTable.setEvaluation(null, squareEvaluation, pieceTypeCoeffs);
	}
	
}
