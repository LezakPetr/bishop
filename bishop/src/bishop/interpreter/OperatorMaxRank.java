package bishop.interpreter;

import bishop.base.BitLoop;
import bishop.base.Rank;
import bishop.base.Square;

public class OperatorMaxRank implements IExpression {
	
	private final IExpression operand;
	
	public OperatorMaxRank(final IExpression operand) {
		this.operand = operand;
	}

	@Override
	public long evaluate(final Context context) {
		return getMaxRank (operand.evaluate(context));
	}

	private long getMaxRank(final long board) {
		int rank = Rank.R1 - 1;
		
		for (BitLoop loop = new BitLoop(board); loop.hasNextSquare(); ) {
			final int square = loop.getNextSquare();
			
			rank = Math.max(rank, Square.getRank(square));
		}
		
		return rank;
	}
	
}
