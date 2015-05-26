package bishop.interpreter;

public interface IExpression {
	/**
	 * Evaluates expression.
	 * @param context context
	 * @return value of the expression
	 */
	public long evaluate(final Context context);
}
