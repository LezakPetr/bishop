package bishop.base;

public class AdditiveMaterialEvaluator implements IMaterialEvaluator {
	
	private static final AdditiveMaterialEvaluator instance = new AdditiveMaterialEvaluator();
	
	private AdditiveMaterialEvaluator() {
	}

	@Override
	public int evaluateMaterial(final IPieceCounts pieceCounts) {
		int evaluation = 0;
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				final int count = pieceCounts.getPieceCount(color, pieceType);
				final int unitEvaluation = PieceTypeEvaluations.getPieceEvaluation(color, pieceType);
				
				evaluation += unitEvaluation * count;
			}
		}
		
		return evaluation;
	}

	public static AdditiveMaterialEvaluator getInstance() {
		return instance;
	}
	
}
