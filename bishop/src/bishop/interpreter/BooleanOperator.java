package bishop.interpreter;

public abstract class BooleanOperator implements IExpression {
	
	public abstract boolean evaluateBoolean (final Context context);
	
	@Override
	public long evaluate(final Context context) {
		return (evaluateBoolean(context)) ? 1 : 0;
	}

}
