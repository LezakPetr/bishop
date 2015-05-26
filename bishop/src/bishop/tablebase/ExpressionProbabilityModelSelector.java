package bishop.tablebase;

import bishop.base.Position;
import bishop.interpreter.Bytecode;
import bishop.interpreter.Context;
import bishop.interpreter.ExpressionCreator;
import bishop.interpreter.IExpression;

public class ExpressionProbabilityModelSelector implements IProbabilityModelSelector {

	private final IExpression expression;
	
	public ExpressionProbabilityModelSelector(final Bytecode bytecode) {
		final ExpressionCreator creator = new ExpressionCreator();
		expression =  creator.createExpression(bytecode);
	}
	
	@Override
	public int getModelCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getModelIndex(final Position position) {
		final Context context = new Context();
		context.setPosition(position);
		
		return expression.evaluate(context);
	}

	@Override
	public void addSymbol(final Position position, final int symbol) {
		// No operation
	}

	@Override
	public void resetSymbols() {
		// No operation
	}

}
