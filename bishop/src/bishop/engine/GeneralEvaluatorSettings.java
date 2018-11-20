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
		
		for (int kingSquare = Square.FIRST; kingSquare < Square.LAST; kingSquare++) {
			lineTables[kingSquare] = calculateLineTableForSquare (kingSquare);
			knightTables[kingSquare] = calculateShortTableForSquare (Piece.WHITE_KNIGHT, kingSquare);
			
			for (int pawnColor = Color.FIRST; pawnColor < Color.LAST; pawnColor++)
				pawnTables[pawnColor][kingSquare] = calculateShortTableForSquare (Piece.withColorAndType(pawnColor, PieceType.PAWN), kingSquare);
		}
		
		for (int whiteKingSquare = Square.FIRST; whiteKingSquare < Square.LAST; whiteKingSquare++) {
			for (int blackKingSquare = Square.FIRST; blackKingSquare < Square.LAST; blackKingSquare++) {
				final AttackEvaluationTableGroup group = new AttackEvaluationTableGroup(
					lineTables[whiteKingSquare], lineTables[blackKingSquare],
					knightTables[whiteKingSquare], knightTables[blackKingSquare],
					pawnTables[Color.BLACK][whiteKingSquare], pawnTables[Color.WHITE][blackKingSquare]
				);
				
				attackTableGroups[whiteKingSquare][blackKingSquare] = group;
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
