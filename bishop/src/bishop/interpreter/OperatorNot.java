package bishop.interpreter;

public final class OperatorNot implements IExpression {
	
	private final IExpression operand;
	
	public OperatorNot(final IExpression operand) {
		this.operand = operand;
	}

	@Override
	public long evaluate(final Context context) {
		return ~operand.evaluate(context);
	}

}
