package bishop.engine;

import bishop.base.BitBoardCombinator;
import bishop.base.BitLoop;
import bishop.base.CrossDirection;
import bishop.base.LineAttackTable;
import bishop.base.LineIndexer;
import bishop.base.Square;

public class LineAttackEvaluationTable {
	
	private final byte[] attackTable;
	
	public LineAttackEvaluationTable(final double[] squareEvaluation) {
		attackTable = new byte[LineIndexer.getLastIndex()];
		
		setTable(squareEvaluation);
	}
	
	public int getAttackEvaluation(final int index) {
		return attackTable[index];
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
					
					attackTable[index] = math.Utils.roundToByte(dblEvaluation);
				}
			}
		}
	}
	
	public static final LineAttackEvaluationTable ZERO_TABLE = new LineAttackEvaluationTable(new double[Square.LAST]);
	
}
