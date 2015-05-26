package bishop.interpreter;

import bishop.base.BitBoard;

public class OperatorLastSquare implements IExpression {
	
	private final IExpression operand;
	
	public OperatorLastSquare(final IExpression operand) {
		this.operand = operand;
	}

	@Override
	public long evaluate(final Context context) {
		return BitBoard.getLastSquare(operand.evaluate(context));
	}

}
