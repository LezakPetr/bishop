package bishop.base;

abstract public class AdditiveMaterialEvaluator implements IMaterialEvaluator {
	@Override
	public int evaluateMaterial(final IMaterialHashRead materialHash) {
		int evaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				final int count = materialHash.getPieceCount(color, pieceType);
				final int unitEvaluation = getPieceEvaluation(color, pieceType);
				
				evaluation += unitEvaluation * count;
			}
		}
		
		return evaluation;
	}
	
	abstract public int getPieceEvaluation (final int color, final int pieceType);
}
