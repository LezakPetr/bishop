package bishop.interpreter;

import bishop.base.BitLoop;
import bishop.base.BoardConstants;

public class OperatorMinDist implements IExpression {
	
	private final IExpression operandA;
	private final IExpression operandB;
	
	public OperatorMinDist(final IExpression operandA, final IExpression operandB) {
		this.operandA = operandA;
		this.operandB = operandB;
	}

	@Override
	public long evaluate(final Context context) {
		return getMinDist (operandA.evaluate(context), operandB.evaluate(context));
	}
	
	private long getMinDist(final long boardA, final long boardB) {
		int minDist = 8;
		
		for (BitLoop loopA = new BitLoop(boardA); loopA.hasNextSquare(); ) {
			final int squareA = loopA.getNextSquare();

			for (BitLoop loopB = new BitLoop(boardB); loopB.hasNextSquare(); ) {
				final int squareB = loopB.getNextSquare();
				final int dist = BoardConstants.getKingSquareDistance(squareA, squareB);
				
				minDist = Math.min(minDist, dist);
			}
		}
		
		return minDist;
	}

}
