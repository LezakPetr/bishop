package bishop.engine;

import bishop.base.*;
import bishop.tables.PawnAttackTable;

public class MoveExtensionEvaluator {
			
	private SearchSettings settings;
	private IMaterialEvaluator materialEvaluator;
	private PieceTypeEvaluations pieceTypeEvaluations;
		
	public int getExtension (final Position targetPosition, final Move move, final int rootMaterialEvaluation, final int beginMaterialEvaluation, final AttackCalculator attackCalculator) {
		final int movingPieceType = move.getMovingPieceType();
		final int onTurn = targetPosition.getOnTurn();
		final int oppositeColor = Color.getOppositeColor(onTurn);
		int extension = 0;
		
		// Passed pawn
		if (movingPieceType == PieceType.PAWN) {
			final int targetSquare = move.getTargetSquare();
			final int rank = Square.getRank(targetSquare);
			final int relativeRank = Rank.getRelative (rank, oppositeColor);
			
			if (relativeRank == Rank.R7)
				extension += settings.getPawnOnSevenRankExtension();

			final long pawnMask = targetPosition.getPiecesMask(oppositeColor, PieceType.PAWN);
					
			if (relativeRank == Rank.R6 && (PawnAttackTable.getItem(oppositeColor, targetSquare) & pawnMask) != 0)
				extension += settings.getProtectingPawnOnSixRankExtension();

			final int promotionPieceType = move.getPromotionPieceType();

			if (relativeRank == Rank.R8 && promotionPieceType == PieceType.QUEEN)
				extension += settings.getPawnOnSevenRankExtension();
		}
		
		// Recapture extension
		final int relativeBeginEvaluation = Evaluation.getRelative(beginMaterialEvaluation, oppositeColor);
		final int relativeRootEvaluation = Evaluation.getRelative(rootMaterialEvaluation, oppositeColor);
		final int beginLoss = relativeRootEvaluation - relativeBeginEvaluation - settings.getRecaptureBeginMinTreshold();

		if (beginLoss >= 0) {
			boolean isRecapture = false;
			
			if (move.getCapturedPieceType() != PieceType.NONE) {
				final int materialEvaluation = materialEvaluator.evaluateMaterial(targetPosition.getMaterialHash());
				final int relativeTargetEvaluation = Evaluation.getRelative(materialEvaluation, oppositeColor);
				final int targetSquare = move.getTargetSquare();
				final int sse = targetPosition.getStaticExchangeEvaluation(onTurn, targetSquare, pieceTypeEvaluations);
				
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
		
		// Figure escape extension
		if (PieceType.isFigure(movingPieceType)) {
			final long cheaperAttackedMask = attackCalculator.getCheaperAttackedMask(onTurn, movingPieceType);
			
			if (BitBoard.containsSquare(cheaperAttackedMask, move.getBeginSquare()) && !BitBoard.containsSquare(cheaperAttackedMask, move.getTargetSquare()))
				extension += settings.getFigureEscapeExtension (movingPieceType);
		}
		
		return extension;
	}

	public SearchSettings getSettings() {
		return settings;
	}

	public void setSettings(final SearchSettings settings) {
		this.settings = settings;
	}

	public IMaterialEvaluator getMaterialEvaluator() {
		return materialEvaluator;
	}

	public void setMaterialEvaluator(IMaterialEvaluator materialEvaluator) {
		this.materialEvaluator = materialEvaluator;
	}

	public PieceTypeEvaluations getPieceTypeEvaluations() {
		return pieceTypeEvaluations;
	}

	public void setPieceTypeEvaluations(PieceTypeEvaluations pieceTypeEvaluations) {
		this.pieceTypeEvaluations = pieceTypeEvaluations;
	}

}
