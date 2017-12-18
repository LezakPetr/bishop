package math;

import java.util.function.DoubleBinaryOperator;

/**
 * BinaryVectorAlgorithm is an algorithm that applies operator to corresponding pairs of elements and then passes the result the processor.
 * @author Ing. Petr Ležák
 */
public interface IBinaryVectorAlgorithm {
	public <P extends IVectorElementProcessor> P processElements(final IVectorRead a, final IVectorRead b, final DoubleBinaryOperator operator, final P processor);
}
