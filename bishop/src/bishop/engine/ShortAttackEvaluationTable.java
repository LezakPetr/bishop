package bishop.engine;

import bishop.base.BitBoard;
import bishop.base.BitLoop;
import bishop.base.Piece;
import bishop.base.Square;
import math.Utils;

public class ShortAttackEvaluationTable {

	private final byte[] attackTable = new byte[Square.LAST];
	private final long nonZeroSquares;
	
	public ShortAttackEvaluationTable(final Piece piece, final double[] attackedSquareEvaluation) {
		nonZeroSquares = setTable(piece, attackedSquareEvaluation);
	}
	
	public int getAttackEvaluation(final int square) {
		return attackTable[square];
	}

	public long getNonZeroSquares() {
		return nonZeroSquares;
	}

	private long setTable(final Piece piece, final double[] squareEvaluation) {
		long nonZeroSquares = BitBoard.EMPTY;

		for (int square = Square.FIRST; square < Square.LAST; square++) {
			final long attackMask = piece.getAttackedSquares(square);
			
			double dblEvaluation = 0.0;
			
			for (BitLoop loop = new BitLoop(attackMask); loop.hasNextSquare(); ) {
				final int targetSquare = loop.getNextSquare();
				dblEvaluation += squareEvaluation[targetSquare];
			}

			final byte byteEvaluation = Utils.roundToByte(dblEvaluation);
			attackTable[square] = byteEvaluation;

			if (byteEvaluation != 0)
				nonZeroSquares |= BitBoard.of(square);
		}

		return nonZeroSquares;
	}

	// Universal zero table - the WHITE_KNIGHT is used just because the constructor needs some piece,
	// with zero evaluations it does not matter which piece is used.
	public static final ShortAttackEvaluationTable ZERO_TABLE = new ShortAttackEvaluationTable(Piece.WHITE_KNIGHT, new double[Square.LAST]);

}
