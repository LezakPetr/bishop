package bishop.interpreter;

import bishop.base.PieceType;

public class OperatorKing implements IExpression {

	@Override
	public long evaluate(final Context context) {
		return PieceType.KING;
	}

}
