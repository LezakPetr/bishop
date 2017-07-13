package bishop.engine;

import bishop.base.BitLoop;
import bishop.base.Piece;
import bishop.base.Square;

public class ShortAttackEvaluationTable {

	private final byte[] attackTable = new byte[Square.LAST];
	
	public ShortAttackEvaluationTable(final Piece piece, final double[] attackedSquareEvaluation) {
		setTable(piece, attackedSquareEvaluation);
	}
	
	public int getAttackEvaluation(final int square) {
		return attackTable[square];
	}

	private void setTable(final Piece piece, final double[] squareEvaluation) {
		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final long attackMask = piece.getAttackedSquares(square);
			
			double dblEvaluation = 0.0;
			
			for (BitLoop loop = new BitLoop(attackMask); loop.hasNextSquare(); ) {
				final int targetSquare = loop.getNextSquare();
				dblEvaluation += squareEvaluation[targetSquare];
			}
			
			attackTable[square] = math.Utils.roundToByte(dblEvaluation);
		}
	}

	// Universal zero table - the WHITE_KNIGHT is used just because the constructor needs some piece,
	// with zero evaluations it does not matter which piece is used.
	public static final ShortAttackEvaluationTable ZERO_TABLE = new ShortAttackEvaluationTable(Piece.WHITE_KNIGHT, new double[Square.LAST]);

}
