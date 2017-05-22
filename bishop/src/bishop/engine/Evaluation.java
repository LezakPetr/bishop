package bishop.engine;

import bishop.base.Color;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Square;

public class Evaluation {
	
	public static final int MATE_MIN = Square.LAST * PieceTypeEvaluations.QUEEN_EVALUATION;
	public static final int MATE_ZERO_DEPTH =  MATE_MIN + (1 << 15);;

	public static final int MAX = MATE_ZERO_DEPTH + 1;
	public static final int MIN = -MAX;
	
	public static final int DRAW = 0;
	
	public static final int BYTES = 3;
	
	public static int getMateEvaluation (final int depth) {
		return MATE_ZERO_DEPTH - depth;
	}

	public static String toString(final int evaluation) {
		if (evaluation >= MATE_MIN) {
			final int depth = (MATE_ZERO_DEPTH - evaluation) / 2 + 1;
			
			return "Mate " + depth;
		}
		
		if (evaluation <= -MATE_MIN) {
			final int depth = (evaluation + MATE_ZERO_DEPTH) / 2;
			
			return "-Mate " + depth;			
		}
		
		// Normal
		final double doubleEvaluation = (double) evaluation / (double) PieceTypeEvaluations.PAWN_EVALUATION;
		
		return String.format("%1$.3f", doubleEvaluation);
	}

	public static int getRelative(final int absoluteEvaluation, final int onTurn) {
		if (onTurn == Color.WHITE)
			return absoluteEvaluation;
		else
			return -absoluteEvaluation;
	}
	
	public static int getAbsolute(final int relativeEvaluation, final int onTurn) {
		if (onTurn == Color.WHITE)
			return relativeEvaluation;
		else
			return -relativeEvaluation;
	}

	public static boolean isLoseMateSearch(final int beta) {
		return beta < -MATE_MIN && beta >= -MATE_ZERO_DEPTH;
	}

	public static boolean isWinMateSearch(final int alpha) {
		return alpha > MATE_MIN && alpha <= MATE_ZERO_DEPTH;
	}
	
}
