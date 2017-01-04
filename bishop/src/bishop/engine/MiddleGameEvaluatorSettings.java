package bishop.engine;


import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.File;
import bishop.base.Rank;
import bishop.base.Square;

public final class MiddleGameEvaluatorSettings {
	private final AttackEvaluationTable[][][] attackTable;
	
	private static final double DEFAULT_ATTACK_EVALUATION = 16;
	private static final double DEFAULT_ATTACK_BASE = 2.0;
	
	public MiddleGameEvaluatorSettings() {
		attackTable = new AttackEvaluationTable[File.LAST][File.LAST][Color.LAST];
		setDefaultTables();
	}
	
	
	private void calculateAttackTable() {
		final AttackEvaluationTable[][] tmpTable = new AttackEvaluationTable[Color.LAST][File.LAST];
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int rank = (color == Color.WHITE) ? Rank.R8 : Rank.R1;
			final AttackEvaluationTable shortCastlingTable = calculateTableForSquare (color, File.FG, rank);
			final AttackEvaluationTable longCastlingTable = calculateTableForSquare (color, File.FB, rank);
			final AttackEvaluationTable centralTable = calculateTableForSquare (color, File.FE, rank);
			
			for (int file = File.FA; file <= File.FC; file++)
				tmpTable[color][file] = longCastlingTable;
			
			for (int file = File.FD; file <= File.FE; file++)
				tmpTable[color][file] = centralTable;

			for (int file = File.FF; file <= File.FH; file++)
				tmpTable[color][file] = shortCastlingTable;
		}
		
		for (int whiteFile = File.FIRST; whiteFile < File.LAST; whiteFile++) {
			for (int blackFile = File.FIRST; blackFile < File.LAST; blackFile++) {
				attackTable[whiteFile][blackFile][Color.WHITE] = tmpTable[Color.WHITE][whiteFile];
				attackTable[whiteFile][blackFile][Color.BLACK] = tmpTable[Color.BLACK][blackFile];
			}
		}
	}
	
	private AttackEvaluationTable calculateTableForSquare(final int color, final int file, final int rank) {
		final int referenceSquare = Square.onFileRank(file, rank);
		final double[] squareEvaluation = new double[Square.LAST];
		
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final int distance = BoardConstants.getKingSquareDistance(square, referenceSquare);
			
			squareEvaluation[square] = DEFAULT_ATTACK_EVALUATION * Math.pow(DEFAULT_ATTACK_BASE, -distance);
		}

		return new AttackEvaluationTable(squareEvaluation);
	}
	
	private void setDefaultTables() {
		calculateAttackTable();
	}

	public AttackEvaluationTable[] getAttackTable(final int whiteKingFile, final int blackKingFile) {
		return attackTable[whiteKingFile][blackKingFile];
	}

}
