package bishop.interpreter;

import bishop.base.BitLoop;
import bishop.base.BoardConstants;

public class OperatorMinEdgeDist implements IExpression {
	
	private final IExpression operand;
	
	public OperatorMinEdgeDist(final IExpression operand) {
		this.operand = operand;
	}

	@Override
	public long evaluate(final Context context) {
		return getMinEdgeDist (operand.evaluate(context));
	}
	
	private long getMinEdgeDist(final long board) {
		int minDist = 4;
		
		for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			final int dist = BoardConstants.getSquareEdgeDistance(square);
			
			minDist = Math.min(minDist, dist);
		}
		
		return minDist;
	}
}
