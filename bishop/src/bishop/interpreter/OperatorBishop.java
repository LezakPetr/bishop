package bishop.interpreter;

import bishop.base.PieceType;

public class OperatorBishop  implements IExpression {

	@Override
	public long evaluate(final Context context) {
		return PieceType.BISHOP;
	}

}
