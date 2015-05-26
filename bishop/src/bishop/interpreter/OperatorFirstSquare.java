package bishop.interpreter;

import bishop.base.BitBoard;

public class OperatorFirstSquare implements IExpression {
	
	private final IExpression operand;
	
	public OperatorFirstSquare(final IExpression operand) {
		this.operand = operand;
	}

	@Override
	public long evaluate(final Context context) {
		return BitBoard.getFirstSquare(operand.evaluate(context));
	}
}
