package bishop.base;

public class DefaultAdditiveMaterialEvaluator extends AdditiveMaterialEvaluator {

	private final PieceTypeEvaluations pieceTypeEvaluations;

	public DefaultAdditiveMaterialEvaluator(final PieceTypeEvaluations pieceTypeEvaluations) {
		this.pieceTypeEvaluations = pieceTypeEvaluations;
	}

	@Override
	public int getPieceEvaluation (final int color, final int pieceType) {
		return pieceTypeEvaluations.getPieceEvaluation(color, pieceType);
	}

}
