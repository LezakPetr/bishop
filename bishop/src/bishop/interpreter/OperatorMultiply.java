package bishop.interpreter;

public class OperatorMultiply implements IExpression {
	
	private final IExpression operandA;
	private final IExpression operandB;
	
	public OperatorMultiply(final IExpression operandA, final IExpression operandB) {
		this.operandA = operandA;
		this.operandB = operandB;
	}

	@Override
	public long evaluate(final Context context) {
		return operandA.evaluate(context) * operandB.evaluate(context);
	}
}
