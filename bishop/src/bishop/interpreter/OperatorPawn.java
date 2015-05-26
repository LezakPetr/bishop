package bishop.interpreter;

import bishop.base.PieceType;

public class OperatorPawn implements IExpression {

	@Override
	public long evaluate(final Context context) {
		return PieceType.PAWN;
	}

}
