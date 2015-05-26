package bishop.interpreter;


public final class OperatorInsertNumber implements IExpression {
	
	private final long value;
	
	public OperatorInsertNumber(final long value) {
		this.value = value;
	}

	@Override
	public long evaluate(final Context context) {
		return value;
	}
	
}
