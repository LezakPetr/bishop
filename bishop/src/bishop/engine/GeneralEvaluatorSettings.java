package bishop.engine;


import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.Piece;
import bishop.base.PieceType;
import bishop.base.Square;

public final class GeneralEvaluatorSettings {
	private final AttackEvaluationTableGroup[][] attackTableGroups = new AttackEvaluationTableGroup[Square.LAST][Square.LAST];
	
	private static final double DEFAULT_ATTACK_EVALUATION = 16;
	private static final double DEFAULT_ATTACK_BASE = 2.0;
	
	private static final int MAX_ATTACK_FILE_DISTANCE = 1;
	private static final int MAX_ATTACK_RANK_DISTANCE = 2;
	
	
	public GeneralEvaluatorSettings() {
		setDefaultTables();
	}
	
	
	private void calculateAttackTable() {
		final LineAttackEvaluationTable[] lineTables = new LineAttackEvaluationTable[Square.LAST];
		final ShortAttackEvaluationTable[] knightTables = new ShortAttackEvaluationTable[Square.LAST];
		final ShortAttackEvaluationTable[][] pawnTables = new ShortAttackEvaluationTable[Color.LAST][Square.LAST];
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			lineTables[square] = calculateLineTableForSquare (square);
			knightTables[square] = calculateShortTableForSquare (Piece.WHITE_KNIGHT, square);
			
			for (int color = Color.FIRST; color < Color.LAST; color++)
				pawnTables[color][square] = calculateShortTableForSquare (Piece.withColorAndType(color, PieceType.PAWN), square);
		}
		
		for (int whiteSquare = Square.FIRST; whiteSquare < Square.LAST; whiteSquare++) {
			for (int blackSquare = Square.FIRST; blackSquare < Square.LAST; blackSquare++) {
				final AttackEvaluationTableGroup group = new AttackEvaluationTableGroup(
					lineTables[whiteSquare], lineTables[blackSquare],
					knightTables[whiteSquare], knightTables[blackSquare],
					pawnTables[Color.BLACK][whiteSquare], pawnTables[Color.WHITE][blackSquare]
				);
				
				attackTableGroups[whiteSquare][blackSquare] = group;
			}
		}
	}
	
	private LineAttackEvaluationTable calculateLineTableForSquare(final int referenceSquare) {
		final double[] squareEvaluation = calculateSquareEvaluation(referenceSquare);

		return new LineAttackEvaluationTable(squareEvaluation);
	}

	private ShortAttackEvaluationTable calculateShortTableForSquare(final Piece piece, final int referenceSquare) {
		final double[] squareEvaluation = calculateSquareEvaluation(referenceSquare);

		return new ShortAttackEvaluationTable(piece, squareEvaluation);
	}

	public double[] calculateSquareEvaluation(final int referenceSquare) {
		final int referenceFile = Square.getFile(referenceSquare);
		final int referenceRank = Square.getRank(referenceSquare);
		
		final double[] squareEvaluation = new double[Square.LAST];
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final int file = Square.getFile (square);
			final int rank = Square.getRank (square);
			
			if (Math.abs(file - referenceFile) <= MAX_ATTACK_FILE_DISTANCE && Math.abs(rank - referenceRank) <= MAX_ATTACK_RANK_DISTANCE) {
				final int distance = BoardConstants.getKingSquareDistance(square, referenceSquare);
				
				squareEvaluation[square] = DEFAULT_ATTACK_EVALUATION * Math.pow(DEFAULT_ATTACK_BASE, -distance);
			}
		}
		return squareEvaluation;
	}
	
	private void setDefaultTables() {
		calculateAttackTable();
	}

	public AttackEvaluationTableGroup getAttackTableGroup(final int whiteKingSquare, final int blackKingSquare) {
		return attackTableGroups[whiteKingSquare][blackKingSquare];
	}

}
