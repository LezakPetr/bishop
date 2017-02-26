package bishop.engine;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.function.Supplier;

import bishop.base.IMaterialEvaluator;
import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Position;
import bishop.base.Rank;
import bishop.tables.FrontSquaresOnSameFileTable;

public final class EndingPositionEvaluator implements IPositionEvaluator {
	
	private static final int MAX_POSITIONAL_EVALUATION = PieceTypeEvaluations.getPawnMultiply (2.0);
	
	private final IMaterialEvaluator materialEvaluator;
	private final TablePositionEvaluator tableEvaluator;
	private final BishopColorPositionEvaluator bishopColorPositionEvaluator;
	private final PawnStructureEvaluator pawnStructureEvaluatorWithFigures;
	private final PawnStructureEvaluator pawnStructureEvaluatorPawnsOnly;
	private final PawnRaceEvaluator pawnRaceEvaluator;
	
	private Position position;
	
	private int materialEvaluation;
	private final IPositionEvaluation evaluation;
	private final IPositionEvaluation positionalEvaluation;
	
	private final boolean[] hasFigures;
	

	public EndingPositionEvaluator(final IMaterialEvaluator materialEvaluator, final PawnStructureCache structureCache, final Supplier<IPositionEvaluation> evaluationFactory) {
		this.materialEvaluator = materialEvaluator;
		this.evaluation = evaluationFactory.get();
		this.positionalEvaluation = evaluationFactory.get();
		
		tableEvaluator = new TablePositionEvaluator(PositionEvaluationCoeffs.ENDING_TABLE_EVALUATOR_COEFFS, evaluationFactory);
		bishopColorPositionEvaluator = new BishopColorPositionEvaluator(evaluationFactory);
		pawnStructureEvaluatorWithFigures  = new PawnStructureEvaluator(PositionEvaluationCoeffs.ENDING_WITH_FIGURES_PAWN_STRUCTURE_COEFFS, structureCache, evaluationFactory);
		pawnStructureEvaluatorPawnsOnly = new PawnStructureEvaluator(PositionEvaluationCoeffs.ENDING_PAWNS_ONLY_PAWN_STRUCTURE_COEFFS, structureCache, evaluationFactory);
		pawnRaceEvaluator = new PawnRaceEvaluator(evaluationFactory);
		
		hasFigures = new boolean[Color.LAST];
	}
		
	private void clear() {
		materialEvaluation = 0;
		
		evaluation.clear();
		positionalEvaluation.clear();
		pawnStructureEvaluatorWithFigures.clear();
		pawnStructureEvaluatorPawnsOnly.clear();
	}
	
	private void calculateHasFigures() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			long mask = BitBoard.EMPTY;
			
			for (int pieceType = PieceType.PROMOTION_FIGURE_FIRST; pieceType < PieceType.PROMOTION_FIGURE_LAST; pieceType++) {
				mask |= position.getPiecesMask(color, pieceType);
			}
			
			hasFigures[color] = (mask != 0);
		}
	}
	
	private void evaluatePawns(final AttackCalculator attackCalculator) {
		// Rule of square
		if (!hasFigures[Color.WHITE] && !hasFigures[Color.BLACK]) {
			evaluation.addSubEvaluation(pawnRaceEvaluator.evaluate(position));
		}
	}
	
	public IPositionEvaluation evaluatePosition(final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		this.position = position;
		
		clear();
		
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);		
		materialEvaluation = materialEvaluator.evaluateMaterial(position);
		
		// Speedup if materialEvaluation is not in <alpha, beta>
		final int lowerBound = materialEvaluation + MAX_POSITIONAL_EVALUATION;
		
		if (lowerBound < alpha) {
			evaluation.addEvaluation(lowerBound);
			return evaluation;
		}
		
		final int upperBound = materialEvaluation - MAX_POSITIONAL_EVALUATION;
		
		if (upperBound > beta) {
			evaluation.addEvaluation(upperBound);
			return evaluation;
		}

		// Regular evaluation
		evaluation.addEvaluation(materialEvaluation);

		calculatePositionalEvaluation(attackCalculator);
		final int positionalEvaluationValue = positionalEvaluation.getEvaluation();
		
		if (positionalEvaluationValue < -MAX_POSITIONAL_EVALUATION) {
			evaluation.addEvaluation(-MAX_POSITIONAL_EVALUATION);
			return evaluation;
		}
		
		if (positionalEvaluationValue > MAX_POSITIONAL_EVALUATION) {
			evaluation.addEvaluation(MAX_POSITIONAL_EVALUATION);
			return evaluation;
		}
		
		evaluation.addSubEvaluation(positionalEvaluation);
		
		return evaluation;
	}
	
	private void calculatePositionalEvaluation(final AttackCalculator attackCalculator) {
		calculateHasFigures();
		
		final PawnStructureEvaluator pawnStructureEvaluator = (hasFigures[Color.WHITE] || hasFigures[Color.BLACK]) ?
				pawnStructureEvaluatorWithFigures : pawnStructureEvaluatorPawnsOnly;
		
		pawnStructureEvaluator.calculate(position);
		
		positionalEvaluation.addSubEvaluation(tableEvaluator.evaluatePosition(position));
		positionalEvaluation.addSubEvaluation(bishopColorPositionEvaluator.evaluatePosition(position));
		
		evaluatePawns(attackCalculator);
		positionalEvaluation.addSubEvaluation(pawnStructureEvaluator.evaluate(position, attackCalculator));
	}

	/**
	 * Writes information about the position into given writer.
	 * @param writer target writer
	 */
	public void writeLog (final PrintWriter writer) {
		bishopColorPositionEvaluator.writeLog(writer);
		
		writer.println ("EndingPositionEvaluator");
		writer.println ("=======================");
		
		writer.println ("Material evaluation: " + Evaluation.toString (materialEvaluation));
		writer.println ("Positional evaluation: " + positionalEvaluation.toString());
		writer.println ("Total evaluation: " + evaluation.toString());
	}
	
}
