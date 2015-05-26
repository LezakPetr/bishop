package bishop.interpreter;

import java.util.HashMap;
import java.util.Map;


public class ExpressionCreator extends BytecodeProcessor {

	private final IExpression[] stack;
	private int stackTop;
	
	public ExpressionCreator() {
		this.stack = new IExpression[256];
	}
	
	public IExpression createExpression(final Bytecode bytecode) {
		stackTop = 0;
		
		processBytecode(bytecode);
		
		if (stackTop != 1)
			throw new RuntimeException("Wrong result stack");
		
		return stack[0];
	}
	
	@Override
	protected void processOperand(final long value) {
		stack[stackTop] = new OperatorInsertNumber(value);
		stackTop++;
	}
	
	@Override
	protected void processOperator(final OperatorRecord record) {
		final int arity = record.getArity();
		final IExpression[] arguments = new IExpression[arity];

		stackTop -= arity;
		
		for (int i = 0; i < arity; i++) {
			arguments[i] = stack[stackTop + i];
		}
		
		stack[stackTop] = record.createExpression(arguments);
		stackTop++;
	}

	@Override
	protected void processSwitch(final int caseCount) {
		final Map<Long, IExpression> caseMap = new HashMap<Long, IExpression>();

		stackTop -= 2 * caseCount;
		
		for (int i = 0; i < caseCount; i++) {
			final IExpression keyExpression = stack[stackTop + 2*i];
			final long key = keyExpression.evaluate(null);
			final IExpression value = stack[stackTop + 2*i + 1];
			
			if (caseMap.containsKey(key))
				throw new RuntimeException("Duplicate switch key: " + key);
			
			caseMap.put(key, value);
		}
		
		stackTop--;
		final IExpression controllingExpression = stack[stackTop];

		stack[stackTop] = new StatementSwitch(controllingExpression, caseMap);
		stackTop++;
	}

	
}
