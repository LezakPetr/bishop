package bishop.interpreter;

import java.util.HashMap;
import java.util.Map;

public class StatementSwitch implements IExpression {

	private final IExpression controllingExpression;
	private final Map<Long, IExpression> caseMap;
	
	
	public StatementSwitch (final IExpression controllingExpression, final Map<Long, IExpression> caseMap) {
		this.controllingExpression = controllingExpression;
		
		this.caseMap = new HashMap<Long, IExpression>();
		this.caseMap.putAll(caseMap);
	}
	
	@Override
	public long evaluate(final Context context) {
		final long controllingValue = controllingExpression.evaluate(context);
		final IExpression result = caseMap.get(controllingValue);
		
		if (result == null)
			throw new RuntimeException("Case " + controllingValue + " does not exists");
		
		return result.evaluate(context);
	}

}
