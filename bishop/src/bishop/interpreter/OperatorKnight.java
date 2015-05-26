package bishop.interpreter;

import bishop.base.PieceType;

public class OperatorKnight implements IExpression {

	@Override
	public long evaluate(final Context context) {
		return PieceType.KNIGHT;
	}

}
