package bishop.engine;


import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public class MobilityPositionEvaluator {
	
	public static final int COEFF_COUNT = PieceType.PROMOTION_FIGURE_COUNT;
	
	private final IPositionEvaluation mobilityEvaluation;

	public MobilityPositionEvaluator (final IPositionEvaluation evaluation) {
		this.mobilityEvaluation = evaluation;
	}
	
	public void evaluatePosition(final Position position, final AttackCalculator attackCalculator) {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
				final int mobility = attackCalculator.getMobility(color, pieceType);
				final int coeff = getCoeffForPieceType(pieceType);
				mobilityEvaluation.addCoeff(coeff, color, mobility);
			}
		}
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
