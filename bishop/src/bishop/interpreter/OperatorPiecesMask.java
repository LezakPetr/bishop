package bishop.interpreter;

public class OperatorPiecesMask implements IExpression {
	
	private final IExpression operandA;
	private final IExpression operandB;
	
	public OperatorPiecesMask(final IExpression operandA, final IExpression operandB) {
		this.operandA = operandA;
		this.operandB = operandB;
	}

	@Override
	public long evaluate(final Context context) {
		final int color = (int) operandA.evaluate(context);
		final int pieceType = (int) operandB.evaluate(context);
		
		return context.getPosition().getPiecesMask(color, pieceType);
	}
	
}
