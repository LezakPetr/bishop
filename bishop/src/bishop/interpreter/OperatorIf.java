package bishop.interpreter;

public class OperatorIf implements IExpression {
	
	private final IExpression operandCondition;
	private final IExpression operandPositive;
	private final IExpression operandNegative;
	
	
	public OperatorIf(final IExpression operandCondition, final IExpression operandPositive, final IExpression operandNegative) {
		this.operandCondition = operandCondition;
		this.operandPositive = operandPositive;
		this.operandNegative = operandNegative;
	}

	@Override
	public long evaluate(final Context context) {
		final long conditionResult = operandCondition.evaluate(context);
		
		if (conditionResult == 0)
			return operandNegative.evaluate(context);
		else
			return operandPositive.evaluate(context);
	}

}
