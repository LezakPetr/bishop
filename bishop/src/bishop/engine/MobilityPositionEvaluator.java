package bishop.engine;

import java.util.function.Supplier;

import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public class MobilityPositionEvaluator {
	
	public static final int COEFF_COUNT = PieceType.PROMOTION_FIGURE_COUNT;
	
	private final IPositionEvaluation mobilityEvaluation;

	public MobilityPositionEvaluator (final Supplier<IPositionEvaluation> evaluationFactory) {
		this.mobilityEvaluation = evaluationFactory.get();
	}
	
	public IPositionEvaluation evaluatePosition(final Position position, final AttackCalculator attackCalculator) {
		mobilityEvaluation.clear();
				
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
				final int mobility = attackCalculator.getMobility(color, pieceType);
				final int coeff = getCoeffForPieceType(pieceType);
				mobilityEvaluation.addCoeff(coeff, color, mobility);
			}
		}
		
		return mobilityEvaluation;
	}

	private static int getCoeffForPieceType(int pieceType) {
		return PositionEvaluationCoeffs.MOBILITY_OFFSET + pieceType - PieceType.PROMOTION_FIGURE_FIRST;
	}
	
	public static int registerCoeffs(final CoeffRegistry registry) {
		final int offset = registry.enterCategory("mobility");
		
		for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
			registry.add(Character.toString(PieceType.toChar(pieceType, false)));
		}
		
		registry.leaveCategory();
		
		return offset;
	}
	
}
