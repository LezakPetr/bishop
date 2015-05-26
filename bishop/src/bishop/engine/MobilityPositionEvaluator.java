package bishop.engine;

import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.Position;

public class MobilityPositionEvaluator {
	
	private final MobilityEvaluatorSettings settings;
	private int mobilityEvaluation;

	public MobilityPositionEvaluator (final MobilityEvaluatorSettings settings) {
		this.settings = settings;
	}
	
	public int evaluatePosition(final Position position, final AttackCalculator attackCalculator) {
		mobilityEvaluation = 0;
				
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
				mobilityEvaluation += attackCalculator.getMobility(color, pieceType) * settings.getMobilityEvaluation (color, pieceType);
			}
		}
		
		return mobilityEvaluation;
	}
	
}
