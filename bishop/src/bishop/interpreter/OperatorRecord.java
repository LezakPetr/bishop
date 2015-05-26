package bishop.interpreter;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public final class OperatorRecord {

	private final byte opcode;
	private final String token;
	private final int arity;
	private final Constructor<? extends IExpression> expressionConstructor;
	
	public OperatorRecord (final int opcode, final String token, final int arity, final Class<? extends IExpression> expressionClass) {
		this.opcode = (byte) opcode;
		this.token = token;
		this.arity = arity;
		
		final Class<?>[] parameters = new Class<?>[arity];
		Arrays.fill(parameters, IExpression.class);
		
		try {
			expressionConstructor = expressionClass.getConstructor(parameters);
		}
		catch (Exception ex) {
			throw new RuntimeException("Constructor not found", ex);
		}
	}

	public byte getOpcode() {
		return opcode;
	}

	public String getToken() {
		return token;
	}
	
	public int getArity() {
		return arity;
	}
	
	public IExpression createExpression(final IExpression[] arguments) {
		try {
			return expressionConstructor.newInstance((Object[]) arguments);
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot create expression", ex);
		}
	}

}
