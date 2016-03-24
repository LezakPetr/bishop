package bishop.engine;

import java.io.PrintWriter;
import java.util.Arrays;

import parallel.Parallel;

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
	
	private final TablePositionEvaluator tableEvaluator;
	private final BishopColorPositionEvaluator bishopColorPositionEvaluator;
	private final PawnStructureEvaluator pawnStructureEvaluator;
	
	private final EndingPositionEvaluatorSettings settings;
	
	private Position position;
	
	private int materialEvaluation;
	private int positionalEvaluation;
	private int evaluation;
	
	private final int[] pawnPromotionDistances;
	private final boolean[] hasFigures;
	private int pawnEvaluation;
	

	public EndingPositionEvaluator(final EndingPositionEvaluatorSettings settings) {
		this.settings = settings;
		
		tableEvaluator = new TablePositionEvaluator(settings.getTableSettings());
		bishopColorPositionEvaluator = new BishopColorPositionEvaluator();
		pawnStructureEvaluator = new PawnStructureEvaluator(settings.getPawnStructureEvaluatorSettings());
		
		pawnPromotionDistances = new int[Color.LAST];
		hasFigures = new boolean[Color.LAST];
	}
		
	private void clear() {
		Arrays.fill(pawnPromotionDistances, Rank.LAST);
		
		materialEvaluation = 0;
		positionalEvaluation = 0;
		evaluation = 0;
		pawnEvaluation = 0;
		
		pawnStructureEvaluator.clear();
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
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			
			final long ownPawnMask = position.getPiecesMask(color, PieceType.PAWN);
			final long oppositePawnMask = position.getPiecesMask(oppositeColor, PieceType.PAWN);
			
			for (BitLoop loop = new BitLoop(ownPawnMask); loop.hasNextSquare(); ) {
				final int square = loop.getNextSquare();
				final long frontSquaresOnThreeFiles = BoardConstants.getFrontSquaresOnThreeFiles(color, square);
				final long frontSquaresOnSameFile = FrontSquaresOnSameFileTable.getItem(color, square);
				
				// Passed pawn
				if ((oppositePawnMask & frontSquaresOnThreeFiles) == 0) {
					// Pawn out of square
					if (!hasFigures[oppositeColor]) {
						final long defendingSquares = RuleOfSquare.getKingDefendingSquares(color, position.getOnTurn(), square);
						final long oppositeKingMask = position.getPiecesMask(oppositeColor, PieceType.KING);
						
						if ((defendingSquares & oppositeKingMask) == 0) {
							int distance = BoardConstants.getPawnPromotionDistance(color, square);
							
							if (color == position.getOnTurn())
								distance--;
							
							distance += BitBoard.getSquareCount(position.getOccupancy() & frontSquaresOnSameFile);
							
							pawnPromotionDistances[color] = Math.min(pawnPromotionDistances[color], distance);
						}
					}
				}
			}
		}
		
		// Rule of square
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int oppositeColor = Color.getOppositeColor(color);
			final int ownDistance = pawnPromotionDistances[color];
			final int oppositeDistance = pawnPromotionDistances[oppositeColor];
			
			if (ownDistance < Rank.LAST && oppositeDistance - ownDistance > 1) {
				pawnEvaluation += settings.getRuleOfSquareBonus(color);
			}
		}
	}
	
	public int evaluatePosition(final Parallel parallel, final Position position, final int alpha, final int beta, final AttackCalculator attackCalculator) {
		this.position = position;
		
		clear();
		
		attackCalculator.calculate(position, AttackEvaluationTable.BOTH_COLOR_ZERO_TABLES);
		
		materialEvaluation = position.getMaterialEvaluation();
		
		final int lowerBound = materialEvaluation + MAX_POSITIONAL_EVALUATION;
		
		if (lowerBound < alpha) {
			evaluation = lowerBound;
			return evaluation;
		}
		
		final int upperBound = materialEvaluation - MAX_POSITIONAL_EVALUATION;
		
		if (upperBound > beta) {
			evaluation = upperBound;
			return evaluation;
		}

		calculatePositionalEvaluation(attackCalculator);
		
		final int reducedPositionalEvaluation = Math.min (Math.max (positionalEvaluation, -MAX_POSITIONAL_EVALUATION), MAX_POSITIONAL_EVALUATION);
		evaluation = materialEvaluation + reducedPositionalEvaluation;

		return evaluation;
	}
	
	private void calculatePositionalEvaluation(final AttackCalculator attackCalculator) {
		calculateHasFigures();
		pawnStructureEvaluator.calculate(position);
		
		positionalEvaluation += tableEvaluator.evaluatePosition(position);
		positionalEvaluation += bishopColorPositionEvaluator.evaluatePosition(position);
		
		evaluatePawns(attackCalculator);
		positionalEvaluation += pawnEvaluation;
		pawnEvaluation += pawnStructureEvaluator.evaluate(position, attackCalculator);
	}

	/**
	 * Writes information about the position into given writer.
	 * @param writer target writer
	 */
	public void writeLog (final PrintWriter writer) {
		tableEvaluator.writeLog(writer);
		bishopColorPositionEvaluator.writeLog(writer);
		pawnStructureEvaluator.writeLog (writer);
		
		writer.println ("EndingPositionEvaluator");
		writer.println ("=======================");
		
		writer.println ("Pawn promotion distances: white " + pawnPromotionDistances[Color.WHITE] + ", black " + pawnPromotionDistances[Color.BLACK]);
		
		writer.println ("Pawn bonus evaluation: " + Evaluation.toString (pawnEvaluation));
		writer.println ("Material evaluation: " + Evaluation.toString (materialEvaluation));
		writer.println ("Positional evaluation: " + Evaluation.toString (positionalEvaluation));
		writer.println ("Total evaluation: " + Evaluation.toString (evaluation));
	}
	
}
