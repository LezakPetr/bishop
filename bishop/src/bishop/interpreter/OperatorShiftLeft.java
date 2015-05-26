package bishop.interpreter;

public class OperatorShiftLeft implements IExpression {
	
	private final IExpression operandA;
	private final IExpression operandB;
	
	public OperatorShiftLeft(final IExpression operandA, final IExpression operandB) {
		this.operandA = operandA;
		this.operandB = operandB;
	}

	@Override
	public long evaluate(final Context context) {
		return operandA.evaluate(context) << operandB.evaluate(context);
	}

}
