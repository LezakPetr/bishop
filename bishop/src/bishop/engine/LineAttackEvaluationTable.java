package bishop.engine;

import bishop.base.*;
import math.Utils;

public class LineAttackEvaluationTable {
	
	private final byte[] attackTable;
	private final long[] nonZeroSquares;
	
	public LineAttackEvaluationTable(final double[] squareEvaluation) {
		attackTable = new byte[LineIndexer.getLastIndex()];
		nonZeroSquares = new long[CrossDirection.LAST];
		
		setTable(squareEvaluation);
	}
	
	public int getAttackEvaluation(final int index) {
		return attackTable[index];
	}

	public long getNonZeroSquares(final int direction) {
		return nonZeroSquares[direction];
	}

	private void setTable(final double[] squareEvaluation) {
		for (int direction = CrossDirection.FIRST; direction < CrossDirection.LAST; direction++) {
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				final long mask = LineIndexer.getDirectionMask(direction, square);
				
				for (BitBoardCombinator combinator = new BitBoardCombinator(mask); combinator.hasNextCombination(); ) {
					final long combination = combinator.getNextCombination();
					final int index = LineIndexer.getLineIndex(direction, square, combination);
					final long attackMask = LineAttackTable.getAttackMask(index);
					
					double dblEvaluation = 0.0;
					
					for (BitLoop loop = new BitLoop(attackMask); loop.hasNextSquare(); ) {
						final int targetSquare = loop.getNextSquare();
						dblEvaluation += squareEvaluation[targetSquare];
					}

					final byte byteEvaluation = Utils.roundToByte(dblEvaluation);
					attackTable[index] = byteEvaluation;

					if (byteEvaluation != 0)
						nonZeroSquares[direction] |= BitBoard.of(square);
				}
			}
		}
	}
	
	public static final LineAttackEvaluationTable ZERO_TABLE = new LineAttackEvaluationTable(new double[Square.LAST]);
	
}
