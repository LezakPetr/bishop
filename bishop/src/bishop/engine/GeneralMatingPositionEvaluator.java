package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.IMaterialEvaluator;
import bishop.base.Color;
import bishop.base.Position;
import bishop.tables.MatedKingEvaluationTable;
import bishop.tables.MatingKingEvaluationTable;

public final class GeneralMatingPositionEvaluator implements IPositionEvaluator {
	
	private final IMaterialEvaluator materialEvaluator;
	private final IPositionEvaluation evaluation;
	
	public GeneralMatingPositionEvaluator (final IMaterialEvaluator materialEvaluator, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.materialEvaluator = materialEvaluator;
		this.evaluation = evaluationFactory.get();
	}
	
	public IPositionEvaluation evaluatePosition (final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		final int matingColor = position.getSideWithMorePieces();
		final int matedColor = Color.getOppositeColor(matingColor);
		final int matedKingSquare = position.getKingPosition(matedColor);
		final int matingKingSquare = position.getKingPosition(matingColor);
		
		final int matedKingEvaluation = MatedKingEvaluationTable.getItem(matedKingSquare);
		final int matingKingEvaluation = MatingKingEvaluationTable.getItem(matedKingSquare, matingKingSquare);
		final int matingSideEvaluation = matedKingEvaluation + matingKingEvaluation;
		
		final int materialEvaluation = materialEvaluator.evaluateMaterial(position);
		
		evaluation.clear();
		evaluation.addEvaluation(materialEvaluation);
		evaluation.addEvaluation(Evaluation.getAbsolute(matingSideEvaluation, matingColor));
		
		return evaluation;
	}
	
	public void writeLog (final PrintWriter writer) {
		writer.println ("General mating evaluation: " + evaluation.toString());
	}

}
