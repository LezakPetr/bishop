package bishop.engine;

import java.io.PrintWriter;

import parallel.Parallel;

import bishop.base.Color;
import bishop.base.Position;
import bishop.tables.MatedKingEvaluationTable;
import bishop.tables.MatingKingEvaluationTable;

public final class GeneralMatingPositionEvaluator implements IPositionEvaluator {
	
	private int evaluation; 
	
	public int evaluatePosition (final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		final int matingColor = position.getSideWithMorePieces();
		final int matedColor = Color.getOppositeColor(matingColor);
		final int matedKingSquare = position.getKingPosition(matedColor);
		final int matingKingSquare = position.getKingPosition(matingColor);
		
		final int matedKingEvaluation = MatedKingEvaluationTable.getItem(matedKingSquare);
		final int matingKingEvaluation = MatingKingEvaluationTable.getItem(matedKingSquare, matingKingSquare);
		final int matingSideEvaluation = matedKingEvaluation + matingKingEvaluation;
		
		evaluation = position.getMaterialEvaluation() + Evaluation.getAbsolute(matingSideEvaluation, matingColor);
		
		return evaluation;
	}
	
	public void writeLog (final PrintWriter writer) {
		writer.println ("General mating evaluation: " + Evaluation.toString (evaluation));
	}

}
