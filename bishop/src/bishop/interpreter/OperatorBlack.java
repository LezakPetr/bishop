package bishop.interpreter;

import bishop.base.Color;

public class OperatorBlack implements IExpression {

	@Override
	public long evaluate(final Context context) {
		return Color.BLACK;
	}

}
