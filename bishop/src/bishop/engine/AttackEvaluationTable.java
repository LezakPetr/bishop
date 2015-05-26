package bishop.engine;

import bishop.base.BitBoard;
import bishop.base.BitBoardCombinator;
import bishop.base.BitLoop;
import bishop.base.Color;
import bishop.base.CrossDirection;
import bishop.base.LineAttackTable;
import bishop.base.LineIndexer;
import bishop.base.PieceTypeEvaluations;
import bishop.base.Square;

public class AttackEvaluationTable {
	
	private final short[][] attackTable;
	
	public AttackEvaluationTable(final double constantEvaluation, final double coeff, final double[] squareEvaluation) {
		attackTable = new short[Color.LAST][LineIndexer.getLastIndex()];
		
		setTable(constantEvaluation, coeff, squareEvaluation);
	}
	
	public int getAttackEvaluation(final int color, final int index) {
		return attackTable[color][index];
	}

	private void setTable(final double constantEvaluation, final double coeff, final double[] squareEvaluation) {
		for (int direction = CrossDirection.FIRST; direction < CrossDirection.LAST; direction++) {
			for (int square = Square.FIRST; square < Square.LAST; square++) {
				final long mask = LineIndexer.getDirectionMask(direction, square);
				
				for (BitBoardCombinator combinator = new BitBoardCombinator(mask); combinator.hasNextCombination(); ) {
					final long combination = combinator.getNextCombination();
					final int whiteIndex = LineIndexer.getLineIndex(direction, square, combination);
					final long attackMask = LineAttackTable.getAttackMask(whiteIndex);
					
					double dblEvaluation = 0.0;
					
					for (BitLoop loop = new BitLoop(attackMask); loop.hasNextSquare(); ) {
						final int targetSquare = loop.getNextSquare();
						dblEvaluation += constantEvaluation + coeff * squareEvaluation[targetSquare];
					}
					
					final int evaluation = PieceTypeEvaluations.getPawnMultiply(dblEvaluation);
					attackTable[Color.WHITE][whiteIndex] = (short) evaluation;
					
					final long mirrorCombination = BitBoard.getMirrorBoard(combination);
					final int oppositeSquare = Square.getOppositeSquare(square);
					final int blackIndex = LineIndexer.getLineIndex(direction, oppositeSquare, mirrorCombination);
					attackTable[Color.BLACK][blackIndex] = (short) -evaluation;
				}
			}
		}
	}
	
	public static final AttackEvaluationTable ZERO_TABLE = new AttackEvaluationTable(0.0, 0.0, new double[Square.LAST]);
}
