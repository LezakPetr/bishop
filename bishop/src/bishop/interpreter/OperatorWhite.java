package bishop.interpreter;

import bishop.base.Color;

public class OperatorWhite implements IExpression {

	@Override
	public long evaluate(final Context context) {
		return Color.WHITE;
	}

}
