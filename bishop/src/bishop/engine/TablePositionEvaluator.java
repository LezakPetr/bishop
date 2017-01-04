package bishop.engine;

import java.util.function.Supplier;

import bishop.base.BitLoop;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public final class TablePositionEvaluator {
	
	private final TablePositionCoeffs coeffs;
	private final IPositionEvaluation evaluation;
	
	public TablePositionEvaluator(final TablePositionCoeffs coeffs, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.evaluation = evaluationFactory.get();
		this.coeffs = coeffs;
	}

	public IPositionEvaluation evaluatePosition (final Position position) {
		evaluation.clear();
		
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

		return evaluation;
	}

}
