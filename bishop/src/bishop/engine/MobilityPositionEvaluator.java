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
	
	public IPositionEvaluation evaluatePosition(final Position position, final AttackCalculator attackCalculator, final int gameStage) {
		mobilityEvaluation.clear();

		for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
			final int mobility = attackCalculator.getMobility(pieceType);
			final int coeff = getCoeffForPieceType(pieceType, gameStage);
			mobilityEvaluation.addCoeff(coeff, Color.WHITE, mobility);
		}
		
		return mobilityEvaluation;
	}

	private static int getCoeffForPieceType(final int pieceType, final int gameStage) {
		return PositionEvaluationCoeffs.MOBILITY_OFFSET + pieceType - PieceType.PROMOTION_FIGURE_FIRST + gameStage * PieceType.PROMOTION_FIGURE_COUNT;
	}
	
	public static int registerCoeffs(final CoeffRegistry registry) {
		final int offset = registry.enterCategory("mobility");

		for (int gameStage = GameStage.FIRST; gameStage < GameStage.LAST; gameStage++) {
			for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
				registry.addCoeff(gameStage + "." + Character.toString(PieceType.toChar(pieceType, false)));
			}
		}
		
		registry.leaveCategory();
		
		return offset;
	}
	
}
