package bishop.engine;

import java.io.PrintWriter;
import java.util.function.Supplier;

import bishop.base.Color;
import bishop.base.Position;
import bishop.tables.MatedKingEvaluationTable;
import bishop.tables.MatingKingEvaluationTable;

public final class MatingPositionEvaluator implements IPositionEvaluator {
	
	private final IPositionEvaluation positionalEvaluation;
	private final IPositionEvaluation tacticalEvaluation;
	private Position position;
	
	public MatingPositionEvaluator (final Supplier<IPositionEvaluation> evaluationFactory) {
		this.tacticalEvaluation = evaluationFactory.get();
		this.positionalEvaluation = evaluationFactory.get();
	}
	
	@Override
	public IPositionEvaluation evaluateTactical (final Position position, final MobilityCalculator mobilityCalculator) {
		this.position = position;

		return tacticalEvaluation;		
	}

	@Override
	public IPositionEvaluation evaluatePositional() {
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
