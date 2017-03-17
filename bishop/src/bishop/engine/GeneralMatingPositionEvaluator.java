package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.Color;
import bishop.base.Position;
import bishop.tables.MatedKingEvaluationTable;
import bishop.tables.MatingKingEvaluationTable;

public final class GeneralMatingPositionEvaluator implements IPositionEvaluator {
	
	private final IPositionEvaluation positionalEvaluation;
	private final IPositionEvaluation tacticalEvaluation;
	private Position position;
	
	public GeneralMatingPositionEvaluator (final Supplier<IPositionEvaluation> evaluationFactory) {
		this.tacticalEvaluation = evaluationFactory.get();
		this.positionalEvaluation = evaluationFactory.get();
	}
	
	@Override
	public IPositionEvaluation evaluateTactical (final Position position, final AttackCalculator attackCalculator) {
		this.position = position;
		
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		return tacticalEvaluation;		
	}

	@Override
	public IPositionEvaluation evaluatePositional (final AttackCalculator attackCalculator) {
		final int matingColor = position.getSideWithMorePieces();
		final int matedColor = Color.getOppositeColor(matingColor);
		final int matedKingSquare = position.getKingPosition(matedColor);
		final int matingKingSquare = position.getKingPosition(matingColor);
		
		final int matedKingEvaluation = MatedKingEvaluationTable.getItem(matedKingSquare);
		final int matingKingEvaluation = MatingKingEvaluationTable.getItem(matedKingSquare, matingKingSquare);
		final int matingSideEvaluation = matedKingEvaluation + matingKingEvaluation;
		
		positionalEvaluation.clear();
		positionalEvaluation.addEvaluation(Evaluation.getAbsolute(matingSideEvaluation, matingColor));
		
		return positionalEvaluation;
	}
	
	public void writeLog (final PrintWriter writer) {
	}

}
