package bishop.base;

import math.Utils;

public final class PieceTypeEvaluations {

	public static final int PAWN_EVALUATION   = 1000;
	public static final int KNIGHT_EVALUATION = 3144;
	public static final int BISHOP_EVALUATION = 3350;
	public static final int ROOK_EVALUATION   = 4877;
	public static final int QUEEN_EVALUATION  = 9595;
	public static final int KING_EVALUATION   = 0;
	
	private static final int[] pieceTypeEvaluations = initializePieceTypeEvaluations();
	private static final int[] pieceEvaluations = initializePieceEvaluations();
	
	
	private static int[] initializePieceTypeEvaluations() {
		final int[] evaluations = new int[PieceType.LAST];
		
		evaluations[PieceType.KING]   = KING_EVALUATION;
		evaluations[PieceType.QUEEN]  = QUEEN_EVALUATION;
		evaluations[PieceType.ROOK]   = ROOK_EVALUATION;
		evaluations[PieceType.BISHOP] = BISHOP_EVALUATION;
		evaluations[PieceType.KNIGHT] = KNIGHT_EVALUATION;
		evaluations[PieceType.PAWN]   = PAWN_EVALUATION;
		
		return evaluations;
	}
	
	private static int[] initializePieceEvaluations() {
		final int[] evaluations = new int[Color.LAST * PieceType.LAST];
		
		for (int pieceType = PieceType.FIRST; pieceType < PieceType.LAST; pieceType++) {
			final int evaluation = getPieceTypeEvaluation(pieceType);
			
			evaluations[getPieceIndex(Color.WHITE, pieceType)] = +evaluation;
			evaluations[getPieceIndex(Color.BLACK, pieceType)] = -evaluation;
		}
		
		return evaluations;
	}
	
	public static int getPieceTypeEvaluation (final int pieceType) {
		return pieceTypeEvaluations[pieceType];
	}

	private static int getPieceIndex (final int color, final int pieceType) {
		return color + (pieceType << Color.BIT_COUNT);
	}

	public static int getPieceEvaluation(final int color, final int pieceType) {
		final int index = getPieceIndex (color, pieceType);
		
		return pieceEvaluations[index];
	}
	
	public static int getPawnMultiply (final double multiplier) {
		return Utils.roundToInt (multiplier * PieceTypeEvaluations.PAWN_EVALUATION);
	}
	
}
