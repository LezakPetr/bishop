package bishop.engine;


import bishop.base.BitBoard;
import bishop.base.BitBoardCombinator;
import bishop.base.BitLoop;
import bishop.base.BoardConstants;
import bishop.base.Color;
import bishop.base.CrossDirection;
import bishop.base.File;
import bishop.base.LineAttackTable;
import bishop.base.LineIndexer;
import bishop.base.PieceType;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Rank;
import bishop.base.Square;

public final class MiddleGameEvaluatorSettings {

	private final int[] rookOnOpenFileBonus;
	private final int[] kingMainProtectionPawnBonus;
	private final int[] kingSecondProtectionPawnBonus;
	private final int[] queenMoveBonus;
	private final int[] kingAttackBonus;
	private final int[] figureOnSecureSquareBonus;
	private final TablePositionEvaluatorSettings tablePositionEvaluatorSettings;
	private final MobilityEvaluatorSettings mobilityEvaluatorSettings;
	private final PawnStructureEvaluatorSettings pawnStructureEvaluatorSettings;
	private final AttackEvaluationTable[][][] attackTable;
	
	private static final double ROOK_ON_OPEN_FILE_BONUS = 0.1;
	private static final double KING_MAIN_PROTECTION_PAWN_BONUS = 0.3;
	private static final double KING_SECOND_PROTECTION_PAWN_BONUS = 0.15;
	private static final double QUEEN_MOVE_BONUS = -0.7;
	private static final double KING_ATTACK_BONUS = 0.05;
	private static final double FIGURE_ON_SECURE_SQUARE_BONUS = 0.2;
	private static final double DEFAULT_ATTACK_COEFF = 0.1;
	private static final double DEFAULT_ATTACK_EXPONENT = -2.0;
	

	public MiddleGameEvaluatorSettings() {
		rookOnOpenFileBonus = new int[Color.LAST];
		kingMainProtectionPawnBonus = new int[Color.LAST];
		kingSecondProtectionPawnBonus = new int[Color.LAST];
		queenMoveBonus = new int[Color.LAST];
		kingAttackBonus = new int[Color.LAST];
		figureOnSecureSquareBonus = new int[Color.LAST];
		attackTable = new AttackEvaluationTable[File.LAST][File.LAST][Color.LAST];
		tablePositionEvaluatorSettings = new TablePositionEvaluatorSettings();
		mobilityEvaluatorSettings = new MobilityEvaluatorSettings();
		pawnStructureEvaluatorSettings = new PawnStructureEvaluatorSettings();
		setDefaultTables();
		calculateBonusTables();
	}
	

	private void setSquareOccupancyTables() {
		double[][] squareEvaluation = new double[PieceType.LAST][];
		
		squareEvaluation[PieceType.PAWN] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 1
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 2
			0.000, 0.000, 0.500, 0.600, 0.600, 0.500, 0.000, 0.000,   // 3
			0.000, 0.000, 0.500, 0.100, 1.000, 0.500, 0.000, 0.000,   // 4
			0.000, 0.000, 0.000, 0.900, 0.900, 0.000, 0.000, 0.000,   // 5
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 6
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 7
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000    // 8
		};
		
		squareEvaluation[PieceType.KNIGHT] = new double[] {
		//     A       B       C       D       E       F       G       H
			-0.400, -0.400, -0.400, -0.400, -0.400, -0.400, -0.400, -0.400,   // 1
			 0.000,  0.100,  0.100,  0.200,  0.200,  0.100,  0.100,  0.000,   // 2
			 0.000,  0.200,  0.500,  0.500,  0.500,  0.500,  0.200,  0.000,   // 3
			 0.000,  0.200,  0.500,  1.000,  1.000,  0.500,  0.200,  0.000,   // 4
			 0.000,  0.200,  0.500,  1.000,  1.000,  0.500,  0.200,  0.000,   // 5
			 0.000,  0.200,  0.500,  0.500,  0.500,  0.500,  0.200,  0.000,   // 6
			 0.000,  0.200,  0.200,  0.200,  0.200,  0.200,  0.200,  0.000,   // 7
			 0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000    // 8
		};
		
		squareEvaluation[PieceType.BISHOP] = new double[] {
		//     A       B       C       D       E       F       G       H
			-0.400, -0.400, -0.400, -0.400, -0.400, -0.400, -0.400, -0.400,   // 1
			 0.200,  0.500,  0.300,  0.400,  0.400,  0.300,  0.500,  0.200,   // 2
			 0.200,  0.300,  0.300,  0.400,  0.400,  0.300,  0.300,  0.200,   // 3
			 0.200,  0.200,  0.200,  0.300,  0.300,  0.200,  0.200,  0.200,   // 4
			 0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000,   // 5
			 0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000,   // 6
			 0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000,   // 7
			 0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000,  0.000    // 8
		};
		
		squareEvaluation[PieceType.ROOK] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.100, 0.100, 0.300, 0.400, 0.400, 0.200, 0.000, 0.000,   // 1
			0.100, 0.100, 0.400, 0.300, 0.300, 0.000, 0.000, 0.000,   // 2
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 3
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 4
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 5
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 6
			1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000,   // 7
			1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000    // 8
		};
		
		squareEvaluation[PieceType.QUEEN] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 1
			0.000, 0.000, 0.000, 0.600, 0.600, 0.000, 0.000, 0.000,   // 2
			0.200, 0.200, 0.200, 0.200, 0.200, 0.200, 0.200, 0.200,   // 3
			0.400, 0.400, 0.200, 0.200, 0.200, 0.200, 0.400, 0.400,   // 4
			0.400, 0.400, 0.200, 0.200, 0.200, 0.200, 0.400, 0.600,   // 5
			0.800, 0.800, 0.800, 0.800, 0.800, 0.800, 0.800, 0.800,   // 6
			1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000,   // 7
			1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000, 1.000    // 8
		};
		
		squareEvaluation[PieceType.KING] = new double[] {
		//    A      B      C      D      E      F      G      H
			0.800, 0.800, 0.700, 0.000, 0.000, 0.000, 1.000, 0.800,   // 1
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 2
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 3
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 4
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 5
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 6
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000,   // 7
			0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000    // 8
		};
		
		final double[] pieceTypeCoeffs = new double[PieceType.LAST];
		
		pieceTypeCoeffs[PieceType.PAWN] = 0.4;
		pieceTypeCoeffs[PieceType.KNIGHT] = 0.05;
		pieceTypeCoeffs[PieceType.BISHOP] = 0.05;
		pieceTypeCoeffs[PieceType.ROOK] = 0.1;
		pieceTypeCoeffs[PieceType.QUEEN] = 0.1;
		pieceTypeCoeffs[PieceType.KING] = 0.3;
		
		tablePositionEvaluatorSettings.setPieceEvaluationTable(squareEvaluation, pieceTypeCoeffs);
	}
	
	
	private void calculateAttackTable() {
		final AttackEvaluationTable[][] tmpTable = new AttackEvaluationTable[Color.LAST][File.LAST];
		
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int rank = (color == Color.WHITE) ? Rank.R1 : Rank.R8;
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
			
			squareEvaluation[square] = 1.0 - Math.pow(distance, DEFAULT_ATTACK_EXPONENT);
		}

		final double colorCoeff = (color == Color.WHITE) ? +1 : -1;

		return new AttackEvaluationTable(0.0, colorCoeff * DEFAULT_ATTACK_COEFF, squareEvaluation);
	}
	
	private void setKingSquareAttackTables() {
		final int evaluation = PieceTypeEvaluations.getPawnMultiply(KING_ATTACK_BONUS);
		
		kingAttackBonus[Color.WHITE] = evaluation;
		kingAttackBonus[Color.BLACK] = -evaluation;
	}
	
	private void setDefaultTables() {
		setSquareOccupancyTables();
		setKingSquareAttackTables();
		calculateAttackTable();
	}
	
	
	private void calculateBonusTables() {
		for (int color = Color.FIRST; color < Color.LAST; color++) {
			final int pawnEvaluation = PieceTypeEvaluations.getPieceEvaluation(color, PieceType.PAWN);
			
			rookOnOpenFileBonus[color] = (int) Math.round (pawnEvaluation * ROOK_ON_OPEN_FILE_BONUS);
			kingMainProtectionPawnBonus[color] = (int) Math.round (pawnEvaluation * KING_MAIN_PROTECTION_PAWN_BONUS);
			kingSecondProtectionPawnBonus[color] = (int) Math.round (pawnEvaluation * KING_SECOND_PROTECTION_PAWN_BONUS);
			queenMoveBonus[color] = (int) Math.round (pawnEvaluation * QUEEN_MOVE_BONUS);
			figureOnSecureSquareBonus[color] = (int) Math.round (pawnEvaluation * FIGURE_ON_SECURE_SQUARE_BONUS);
		}
	}
	
	public int getRookOnOpenFileBonus (final int color) {
		return rookOnOpenFileBonus[color];
	}

	public int getKingMainProtectionPawnBonus (final int color) {
		return kingMainProtectionPawnBonus[color];
	}

	public int getKingSecondProtectionPawnBonus (final int color) {
		return kingSecondProtectionPawnBonus[color];
	}

	public int getQueenMoveBonus(final int color) {
		return queenMoveBonus[color];
	}
	
	public int getKingAttackBonus(final int color) {
		return kingAttackBonus[color];
	}
	
	public TablePositionEvaluatorSettings getTablePositionEvaluatorSettings() {
		return tablePositionEvaluatorSettings;
	}

	public MobilityEvaluatorSettings getMobilityEvaluatorSettings() {
		return mobilityEvaluatorSettings;
	}
	
	public PawnStructureEvaluatorSettings getPawnStructureEvaluatorSettings() {
		return pawnStructureEvaluatorSettings;
	}

	public int getFigureOnSecureSquareBonus(final int color) {
		return figureOnSecureSquareBonus[color];
	}

	public AttackEvaluationTable[] getAttackTable(final int whiteKingFile, final int blackKingFile) {
		return attackTable[whiteKingFile][blackKingFile];
	}

}
