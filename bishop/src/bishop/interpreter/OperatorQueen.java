package bishop.interpreter;

import bishop.base.PieceType;

public class OperatorQueen implements IExpression {

	@Override
	public long evaluate(final Context context) {
		return PieceType.QUEEN;
	}

}
