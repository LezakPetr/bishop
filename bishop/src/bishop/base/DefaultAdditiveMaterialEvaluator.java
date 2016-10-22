package bishop.base;

public class DefaultAdditiveMaterialEvaluator extends AdditiveMaterialEvaluator {
	
	private static final DefaultAdditiveMaterialEvaluator instance = new DefaultAdditiveMaterialEvaluator();
	
	private DefaultAdditiveMaterialEvaluator() {
	}

	@Override
	public int getPieceEvaluation (final int color, final int pieceType) {
		return PieceTypeEvaluations.getPieceEvaluation(color, pieceType);
	}

	public static DefaultAdditiveMaterialEvaluator getInstance() {
		return instance;
	}
	
}
