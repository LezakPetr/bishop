package bishop.interpreter;

public final class OperatorOnTurn implements IExpression {
	
	@Override
	public long evaluate(final Context context) {
		return context.getPosition().getOnTurn();
	}
}
