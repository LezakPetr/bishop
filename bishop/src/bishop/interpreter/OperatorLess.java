package bishop.interpreter;

public class OperatorLess extends BooleanOperator {
	
	private final IExpression operandA;
	private final IExpression operandB;
	
	public OperatorLess(final IExpression operandA, final IExpression operandB) {
		this.operandA = operandA;
		this.operandB = operandB;
	}

	@Override
	public boolean evaluateBoolean(final Context context) {
		return operandA.evaluate(context) < operandB.evaluate(context);
	}

}
