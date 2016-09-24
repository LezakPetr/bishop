package bishop.engine;

import bishop.base.AdditiveMaterialEvaluator;
import bishop.base.Color;
import bishop.base.Move;
import bishop.base.PieceType;
import bishop.base.Position;
import bishop.base.Rank;
import bishop.base.Square;

public class MoveExtensionEvaluator {
			
	private SearchSettings settings;
		
	public int getExtension (final Position targetPosition, final Move move, final int rootMaterialEvaluation, final int beginMaterialEvaluation) {
		final int movingPieceType = move.getMovingPieceType();
		final int onTurn = targetPosition.getOnTurn();
		final int oppositeColor = Color.getOppositeColor(onTurn);
		int extension = 0;
		
		// Passed pawn
		if (movingPieceType == PieceType.PAWN) {
			final int rank = Square.getRank(move.getTargetSquare());
			
			if (rank == Rank.R7 || rank == Rank.R2)
				extension += settings.getPawnOnSevenRankExtension();

			final int promotionPieceType = move.getPromotionPieceType();

			if ((rank == Rank.R8 || rank == Rank.R1) && promotionPieceType == PieceType.QUEEN)
				extension += settings.getPawnOnSevenRankExtension();
		}
		
		// Recapture extension
		final int relativeBeginEvaluation = Evaluation.getRelative(beginMaterialEvaluation, oppositeColor);
		final int relativeRootEvaluation = Evaluation.getRelative(rootMaterialEvaluation, oppositeColor);
		final int beginLoss = relativeRootEvaluation - relativeBeginEvaluation - settings.getRecaptureBeginMinTreshold();

		if (beginLoss >= 0) {
			boolean isRecapture = false;
			
			if (move.getCapturedPieceType() != PieceType.NONE) {
				final int materialEvaluation = AdditiveMaterialEvaluator.getInstance().evaluateMaterial(targetPosition);
				final int relativeTargetEvaluation = Evaluation.getRelative(materialEvaluation, oppositeColor);
				final int targetSquare = move.getTargetSquare();
				final int sse = targetPosition.getStaticExchangeEvaluation(onTurn, targetSquare);
				
				if (relativeTargetEvaluation - sse - relativeRootEvaluation >= -settings.getRecaptureTargetTreshold()) {
					isRecapture = true;
				}
			}
			
			if (isRecapture) {
				final int recaptureNumerator = settings.getRecaptureMaxExtension() - settings.getRecaptureMinExtension();
				final int recaptureDenominator = settings.getRecaptureBeginMaxTreshold() - settings.getRecaptureBeginMinTreshold();
				final int effectiveBeginLoss = Math.min(beginLoss, recaptureDenominator);
				final int recaptureExtension = settings.getRecaptureMinExtension() + (effectiveBeginLoss * recaptureNumerator) / recaptureDenominator;
				
				extension += recaptureExtension;
			}
		}
		
		return extension;
	}

	public SearchSettings getSettings() {
		return settings;
	}

	public void setSettings(final SearchSettings settings) {
		this.settings = settings;
	}
}
