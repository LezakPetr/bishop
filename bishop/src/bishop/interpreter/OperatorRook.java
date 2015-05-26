package bishop.interpreter;

import bishop.base.PieceType;

public class OperatorRook implements IExpression {

	@Override
	public long evaluate(final Context context) {
		return PieceType.ROOK;
	}


}
