package bishop.engine;


import bishop.base.BitLoop;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public final class TablePositionEvaluator {
	
	private final TablePositionFeatures coeffs;
	private final IPositionEvaluation evaluation;
	
	public TablePositionEvaluator(final TablePositionFeatures coeffs, final IPositionEvaluation evaluation) {
		this.evaluation = evaluation;
		this.coeffs = coeffs;
	}

	public void evaluatePosition (final Position position) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
				final long board = position.getPiecesMask(color, pieceType);
				
				for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
					final int square = loop.getNextSquare();
					final int coeff = coeffs.getCoeff(color, pieceType, square);
					
					evaluation.addCoeff(coeff, color);
				}
			}
		}
	}

}
