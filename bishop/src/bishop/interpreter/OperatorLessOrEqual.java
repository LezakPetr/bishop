package bishop.interpreter;

public class OperatorLessOrEqual extends BooleanOperator {
	
	private final IExpression operandA;
	private final IExpression operandB;
	
	public OperatorLessOrEqual(final IExpression operandA, final IExpression operandB) {
		this.operandA = operandA;
		this.operandB = operandB;
	}

	@Override
	public boolean evaluateBoolean(final Context context) {
		return operandA.evaluate(context) <= operandB.evaluate(context);
	}
}
