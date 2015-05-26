package bishop.interpreter;

public final class OperatorNegate implements IExpression {
	
	private final IExpression operand;
	
	public OperatorNegate(final IExpression operand) {
		this.operand = operand;
	}

	@Override
	public long evaluate(final Context context) {
		return -operand.evaluate(context);
	}

}
