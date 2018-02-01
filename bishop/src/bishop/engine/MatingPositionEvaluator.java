package bishop.engine;

import java.io.PrintWriter;

import bishop.base.Color;
import bishop.base.Position;
import bishop.tables.MatedKingEvaluationTable;
import bishop.tables.MatingKingEvaluationTable;

public final class MatingPositionEvaluator implements IPositionEvaluator {
	
	private final IPositionEvaluation evaluation;
	
	public MatingPositionEvaluator (final IPositionEvaluation evaluation) {
		this.evaluation = evaluation;
	}
	
	@Override
	public void evaluate (final Position position, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTableGroup.ZERO_GROUP);
		
		final int matingColor = position.getSideWithMorePieces();
		final int matedColor = Color.getOppositeColor(matingColor);
		final int matedKingSquare = position.getKingPosition(matedColor);
		final int matingKingSquare = position.getKingPosition(matingColor);
		
		final int matedKingEvaluation = MatedKingEvaluationTable.getItem(matedKingSquare);
		final int matingKingEvaluation = MatingKingEvaluationTable.getItem(matedKingSquare, matingKingSquare);
		final int matingSideEvaluation = matedKingEvaluation + matingKingEvaluation;
		
		evaluation.addCoeffWithCount(PositionEvaluationCoeffs.EVALUATION_COEFF, Evaluation.getAbsolute(matingSideEvaluation, matingColor));
	}
	
	public void writeLog (final PrintWriter writer) {
	}

	@Override
	public IPositionEvaluation getEvaluation() {
		return evaluation;
	}

}
