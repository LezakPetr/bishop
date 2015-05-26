package bishop.interpreter;

import bishop.base.BitLoop;
import bishop.base.Rank;
import bishop.base.Square;

public class OperatorMinRank implements IExpression {
	
	private final IExpression operand;
	
	public OperatorMinRank(final IExpression operand) {
		this.operand = operand;
	}

	@Override
	public long evaluate(final Context context) {
		return getMinRank (operand.evaluate(context));
	}
	
	private static long getMinRank(final long board) {
		int rank = Rank.R8 + 1;
		
		for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			
			rank = Math.min(rank, Square.getRank(square));
		}
		
		return rank;
	}
	
}
