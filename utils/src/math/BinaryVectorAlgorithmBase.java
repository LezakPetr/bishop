package math;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;

abstract public class BinaryVectorAlgorithmBase implements IBinaryVectorAlgorithm {

	protected static void checkPreconditions(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final IVectorElementProcessor processor) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);
		Objects.requireNonNull(operator);
		Objects.requireNonNull(processor);
		
		if (a.getDimension() != b.getDimension())
			throw new RuntimeException("Dimensions of input vectors does not match, they are " + a.getDimension() + ", " + b.getDimension());
	}
}
